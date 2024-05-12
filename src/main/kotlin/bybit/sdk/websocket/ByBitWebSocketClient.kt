package bybit.sdk.websocket

import bybit.sdk.*
import bybit.sdk.shared.sha256_HMAC
import bybit.sdk.websocket.ByBitWebSocketMessage.*
import com.unciv.utils.Concurrency
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

private const val TOPIC_MESSAGE_KEY = "topic"
private const val OPERATION_MESSAGE_KEY = "op"

enum class ByBitEndpoint(internal vararg val pathComponents: String) {
    Spot("v5", "public", "spot"),
    Linear("v5", "public", "linear"),
    Inverse("v5", "public", "inverse"),
    Option("v5", "public", "option"),
    Private("v5", "private")
}


data class WSClientConfigurableOptions(
    val endpoint: ByBitEndpoint,
    val key: String? = null,
    val secret: String? = null,
    val testnet: Boolean = true,
    val pingInterval: Long = 10_000L,
    val websocketPingTimeout: Long = 4_000L,
    val name: String = "unnamed"
) {
    constructor(endpoint: ByBitEndpoint, testnet: Boolean) : this(endpoint, null, null, testnet) {

    }
}

/**
 *
 * @param apiKey the API key to use with all API requests
 * @param secret the API secret to use with all API requests
 * @param endpoint the [ByBitEndpoint] to connect to
 * @param httpClientProvider (Optional) A provider for the ktor [HttpClient] to use;
 */
class ByBitWebSocketClient
@JvmOverloads
constructor(
    val options: WSClientConfigurableOptions,
    val httpClientProvider: HttpClientProvider = DefaultWebSocketClientProvider(),
) {

    internal val client = httpClientProvider.buildClient()

    /** Channel to send frames via WebSocket to the server, may be null
     * for unsupported servers or unauthenticated/uninitialized clients */
    private var sendChannel: SendChannel<Frame>? = null

    /** List of channel that extend the usage of the [EventBus] system, see [getWebSocketEventChannel] */
    private val eventChannelList = mutableListOf<SendChannel<ByBitWebSocketMessage>>()

    protected var websocketJobs = ConcurrentLinkedQueue<Job>()

    private val logger = Logging.getLogger(ByBitWebSocketClient::class)

    private var reconnectWebSocket = true

    private val serializer by lazy {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    /** Map of waiting receivers of pongs (answers to pings) via a channel that gets null
     * or any thrown exception; access is synchronized on this instance */
    private val pongReceivers: MutableMap<String, Channel<Exception?>> = mutableMapOf()


    /**
     * Set this to true if you want to parse incoming messages yourself
     */
    var sendRaw: Boolean = false

    private val _subscriptions = mutableListOf<ByBitWebSocketSubscription>()

    var isAuthenticated = AtomicBoolean(false)

    /**
     * Enable auto re-connect attempts for the WebSocket connection
     */
    fun enableReconnecting() {
        reconnectWebSocket = true
    }

    /**
     * Disable auto re-connect attempts for the WebSocket connection
     */
    fun disableReconnecting() {
        reconnectWebSocket = false
    }

    fun getWebSocketEventChannel(capacity: Int, onBufferOverflow: BufferOverflow): ReceiveChannel<ByBitWebSocketMessage> {
        // CONFLATED means events might drop, UNLIMITED should use lots of memory
        logger.warn("we are creating a Channel for websocket which capacity = $capacity")
        val c = Channel<ByBitWebSocketMessage>(
            capacity = capacity,
            onBufferOverflow = onBufferOverflow,
            onUndeliveredElement = { value -> logger.warn("Dropped value: $value") }  )
        eventChannelList.add(c as SendChannel<ByBitWebSocketMessage>)
        return c
    }


    /**
     * Start a new WebSocket connection
     *
     * The parameter [handler] is a coroutine that will be fed the established
     * [ClientWebSocketSession] on success at a later point. Note that this
     * method does instantly return, detaching the creation of the WebSocket.
     * The [handler] coroutine might not get called, if opening the WS fails.
     * Use [jobCallback] to receive the newly created job handling the WS connection.
     */
    internal suspend fun websocket(
        handler: suspend (ClientWebSocketSession) -> Unit,
        jobCallback: ((Job) -> Unit)? = null
    ): Boolean {
        logger.debug("Starting a new WebSocket connection (${options.name}) ...")

        coroutineScope {
            try {

                val session = client.webSocketRawSession {
//                    authHelper.add(this)
                    host = if (options.testnet) {
                        "stream-testnet.bybit.com"
                    } else {
                        "stream.bybit.com"
                    }
                    url {
                        protocol = URLProtocol.WSS
                        port = URLProtocol.WSS.defaultPort
                        path(*options.endpoint.pathComponents)
                    }
                    headers["User-Agent"] = Version.userAgent
                }
                val job = Concurrency.runOnNonDaemonThreadPool {
                    handler(session)
                }
                websocketJobs.add(job)
                logger.debug("A new WebSocket has been created, running in job {}", job)
                if (jobCallback != null) {
                    jobCallback(job)
                }
                true
            } catch (e: SerializationException) {
                logger.debug("Failed to create a WebSocket: {}", e.localizedMessage)
                return@coroutineScope false
            } catch (e: Exception) {
                logger.debug("Failed to establish WebSocket connection: {}", e.localizedMessage)
                return@coroutineScope false
            }
        }

        return true
    }

    /**
     * Handle a newly established WebSocket connection
     */
    private suspend fun handleWebSocket(session: ClientWebSocketSession) {
        isAuthenticated.set(false)
        sendChannel?.close()
        sendChannel = session.outgoing

        websocketJobs.add(Concurrency.run {
            val currentChannel = session.outgoing
            while (sendChannel != null && currentChannel == sendChannel) {
                try {
//                    sendPing()
                    val pingTime = awaitPing(timeout = options.websocketPingTimeout)

                    if (pingTime == null) {
                        logger.debug { "null pingTime (${options.name})" }
                        sendChannel?.close()
                    } else {
                        logger.debug { "--- PING TIME: $pingTime (${options.name})" }
                    }
                } catch (e: Exception) {
                    logger.debug("Failed to send WebSocket ping(${options.name}): {}", e.localizedMessage)
                    Concurrency.run {
                        if (reconnectWebSocket) {
                            websocket(::handleWebSocket)
                        }
                    }
                }
                delay(options.pingInterval)
            }
            logger.debug("It looks like the WebSocket channel has been replaced (${options.name})")
        })


        //this ensures we get our "snapshot" when subscribing to the stream (ie. full orderbook)
        val scope = CoroutineScope(Dispatchers.Default + Job())
        scope.launch {
            delay(10000)
            subscribe(immediately = true)
        }

        try {
            while (true) {
                val incomingFrame = session.incoming.receive()
                when (incomingFrame.frameType) {
                    FrameType.PING -> {
                        sendPong(incomingFrame.data)
                    }

                    FrameType.PONG -> {
                        logger.warn(
                            "Received real PONG frame, but this is not supported: {}", incomingFrame.data
                        )
                    }

                    FrameType.CLOSE -> {
                        logger.warn { "Received FrameType.CLOSE for session.incoming.receive()"}
                        throw ClosedReceiveChannelException("Received CLOSE frame via WebSocket")
                    }

                    FrameType.BINARY -> {
                        logger.debug(
                            "Received binary packet of size {} which can't be parsed at the moment",
                            incomingFrame.data.size
                        )
                    }

                    FrameType.TEXT -> {
                        try {

                            if (!isAuthenticated.get()) {
                                if (parseAuthenticationFrame(incomingFrame)) {
                                    return
                                }
                            }

                            val messageList: List<ByBitWebSocketMessage> = if (sendRaw) {
                                listOf(RawMessage(incomingFrame.toString()))
                            } else {
                                val json = serializer.parseToJsonElement(String(incomingFrame.readBytes()))
                                processFrameJson(json)
                            }

                            for (msg in messageList) {
                                logger.trace("Incoming WebSocket message {}: {}", msg::class.java.canonicalName, msg)

                                if (eventChannelList.isEmpty()) {
                                    logger.error("eventChannelList is empty")
                                    continue
                                }

                                for (c in eventChannelList) {
                                    Concurrency.run {
                                        try {
                                            when (msg) {
                                                is TopicResponse.PublicTrade -> {
                                                    // We're OK to drop trades in times of high volatility
                                                    // trySend -> isFailure when capacity is full
                                                    // only the ByBitLiveFeed gets PublicTrade messages
                                                    val channelResult = c.trySend(msg)
                                                    if (channelResult.isFailure) {
                                                        logger.warn("trySend failed: $channelResult")
                                                    }
                                                }
                                                else -> {
                                                    c.send(msg)
                                                }
                                            }
                                        } catch (e: ClosedSendChannelException) {
                                            logger.error { "SendChannel closed: ${e.message}"}
                                            delay(10)
                                            eventChannelList.remove(c)
                                        } catch (t: Throwable) {
                                            logger.debug(
                                                "Sending event {} to event channel {} failed: {}", msg.toString(), c, t
                                            )
                                            delay(10)
                                            eventChannelList.remove(c)
                                        }
                                    }
                                }
                            }

                        } catch (e: Throwable) {
                            logger.error("{}}\n{}", e.localizedMessage, e.stackTraceToString())
                        }
                    }
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.debug("The WebSocket channel was closed: $e")
            sendChannel?.close()
            session.close()
            session.flush()
            Concurrency.run {
                if (reconnectWebSocket) {
                    websocket(::handleWebSocket)
                }
            }
        } catch (e: CancellationException) {
            logger.debug("WebSocket coroutine was cancelled, closing connection: $e")
            sendChannel?.close()
            session.close()
            session.flush()
        } catch (e: Throwable) {
            logger.error(
                "Error while handling a WebSocket connection: {}\n{}",
                e.localizedMessage,
                e.stackTraceToString()
            )
            sendChannel?.close()
            session.close()
            session.flush()
            Concurrency.run {
                if (reconnectWebSocket) {
                    websocket(::handleWebSocket)
                }
            }
//            throw e
        }
    }


    /**
     * Send a [FrameType.PING] and await the response of a [FrameType.PONG]
     *
     * The function returns the delay between Ping and Pong in milliseconds.
     * Note that the function may never return if the Ping or Pong packets are lost on
     * the way, unless [timeout] is set. It will then return `null` if the [timeout]
     * of milliseconds was reached or the sending of the ping failed. Note that ensuring
     * this limit is on a best effort basis and may not be reliable, since it uses
     * [delay] internally to quit waiting for the result of the operation.
     * This function may also throw arbitrary exceptions for network failures.
     */
    suspend fun awaitPing(size: Int = 6, timeout: Long? = null): Double? {
        require(size > 2) { "Size too small to identify ping responses uniquely" }
        logger.trace { "awaitPing($size, $timeout)" }
        val body = ByteArray(size)
        Random().nextBytes(body)

        val key = body.toHex()
        val channel = Channel<Exception?>(capacity = Channel.RENDEZVOUS)
        synchronized(this) {
            pongReceivers[key] = channel
        }

        var job: Job? = null
        if (timeout != null) {
            job = Concurrency.run {
                delay(timeout)
                channel.close()
            }
        }

        try {
            return kotlin.system.measureNanoTime {
                if (!sendPing(key)) {
                    return null
                }
                val exception = runBlocking { channel.receive() }
                job?.cancel(CancellationException("Received ping in time!"))
                channel.close()
                if (exception != null) {
                    throw exception
                }
            }.toDouble() / 10e6
        } catch (_: ClosedReceiveChannelException) {
            logger.trace {"closedReceiveChannel Exception when trying to await Ping: $key"}
            return null
        } catch (e: Throwable) {
            logger.trace {"caught throwable: $e"}
            return null
        } finally {
            synchronized(this) {
                pongReceivers.remove(key)
            }
        }
    }

    /**
     * Handler for incoming [FrameType.PONG] frames to make [awaitPing] work properly
     */
    private suspend fun onPong(content: String) {
        logger.trace {"onPong($content)"}
        val receiver = synchronized(this) {
            pongReceivers[content]
        }
        receiver?.send(null)
    }

    /**
     * Ensure that the WebSocket is connected (send a PING and build a new connection on failure)
     *
     * Use [jobCallback] to receive the newly created job handling the WS connection.
     * Note that this callback might not get called if no new WS connection was created.
     * It returns the measured round trip time in milliseconds if everything was fine.
     */
    suspend fun ensureConnectedWebSocket(
        timeout: Long = options.websocketPingTimeout,
        jobCallback: ((Job) -> Unit)? = null
    ): Double? {
        val pingMeasurement = try {
            awaitPing( timeout = timeout)
        } catch (e: Exception) {
            logger.debug("Error {} while ensuring connected WebSocket: {}", e, e.localizedMessage)
            null
        }
        if (pingMeasurement == null) {
            websocket(::handleWebSocket, jobCallback)
        }
        return pingMeasurement
    }

    suspend fun connect(subscriptions: List<ByBitWebSocketSubscription>) {
        subscriptions.forEach{
            if (!_subscriptions.contains(it)) {
                _subscriptions.add(it)
            }
        }
        logger.debug { "initial connect(${options.endpoint})..." }
        enableReconnecting()
        websocket(::handleWebSocket)
        delay(10_000)
        ensureConnectedWebSocket()
    }

    /**
     * Send a [FrameType.PING] frame with the specified content to the server, without awaiting a response
     *
     * This operation might fail with some exception, e.g. network exceptions.
     * It returns true when sending worked as expected, false when there's no
     * send channel available and an exception otherwise.
     */
    private suspend fun sendPing(content: String): Boolean {
        logger.trace { "sendPing($content)" }
        val channel = sendChannel
        return if (channel == null) {
            false
        } else {
            channel.send(Frame.Text("""{"req_id": "$content", "op": "ping"}"""))
            true
        }
    }

    /**
     * Send a [FrameType.PING] frame to the server, without awaiting a response
     *
     * This operation might fail with some exception, e.g. network exceptions.
     * Internally, a random byte array of [size] will be used for the ping. It
     * returns true when sending worked as expected, false when there's no
     * send channel available and an exception otherwise.
     */
    private suspend fun sendPing(size: Int = 6): Boolean {
        logger.trace {"sendPing(${size})"}
        val body = ByteArray(size)
        Random().nextBytes(body)
        return sendPing(body.toString())
    }

    /**
     * Send a [FrameType.PONG] frame with the specified content to the server
     *
     * This operation might fail with some exception, e.g. network exceptions.
     * It returns true when sending worked as expected, false when there's no
     * send channel available and an exception otherwise.
     */
    private suspend fun sendPong(content: ByteArray): Boolean {
        val channel = sendChannel
        return if (channel == null) {
            false
        } else {
            channel.send(Frame.Pong(content))
            true
        }
    }

    /**
     * Send text as a [FrameType.TEXT] frame to the server via WebSocket (fire & forget)
     *
     * Use [suppress] to forbid throwing *any* errors (returns false, otherwise true or an error).
     *
     * @throws UncivNetworkException: thrown for any kind of network error or de-serialization problems
     */
    @Suppress("Unused")
    internal suspend fun sendText(text: String, suppress: Boolean = false): Boolean {
        val channel = sendChannel
        if (channel == null) {
            logger.debug("No WebSocket connection, can't send text frame to server: '$text'")
            if (suppress) {
                return false
            } else {
                throw Exception("WebSocket not connected", null)
            }
        }
        try {
            channel.send(Frame.Text(text))
        } catch (e: Throwable) {
            logger.debug("Sending text via WebSocket failed: {}\n{}", e.localizedMessage, e.stackTraceToString())
            if (!suppress) {
                throw e
            } else {
                return false
            }
        }
        return true
    }

    /**
     * Subscribe to one or more data streams. Be sure to subscribe to streams available in the
     * current cluster.
     *
     */
    suspend fun subscribe(subscriptions: List<ByBitWebSocketSubscription>? = null, immediately: Boolean = false) {

        val subsToAddNow = subscriptions ?: _subscriptions

        subscriptions?.forEach{
            if (!_subscriptions.contains(it)) {
                _subscriptions.add(it)
            }
        }

        if (!immediately) return

        if (subsToAddNow.isEmpty()) return

        if (
            subsToAddNow.map { it.topic }.filterIsInstance<ByBitWebsocketTopic.PrivateTopic>()
                .isNotEmpty() && !isAuthenticated.get()
        ) {
            require(options.endpoint.equals(ByBitEndpoint.Private)) { "Endpoint must be 'Private' to subscribe to private topics!" }
            val apiKey = options.key
            val expires = Instant.now().toEpochMilli() + 10000

            val toEncode = "GET/realtime$expires"

            val signature = sha256_HMAC(toEncode, options.secret)

            sendText(
                """
                    {"op": "auth",
                        "args":["${
                    arrayOf(
                        apiKey,
                        expires,
                        signature
                    ).joinToString(separator = "\",\"")
                }"]}"""
            )
            delay(400)
        }
        logger.info { "T: Subscribing to:  ${subsToAddNow.joinToString(" , ")}" }
        sendText("""{"op": "subscribe", "args":["${subsToAddNow.joinToString(separator = "\",\"")}"]}""")
    }

    /**
     * Unsubscribe to one or more data streams
     *
     * Calling from Java? See [unsubscribeBlocking] and [unsubscribeAsync]
     */
    suspend fun unsubscribe(subscriptions: List<ByBitWebSocketSubscription>) {
        if (subscriptions.isEmpty()) return

        sendText("""{"op": "unsubscribe", "args":["${subscriptions.joinToString(separator = "\",\"")}"]}""")
    }

    /**
     * Disconnect this client from the websocket server.
     *
     * Calling from Java? See [disconnectBlocking] and [disconnectAsync]
     */
    fun disconnect() {
        disableReconnecting()

        sendChannel?.close()

        for (channel in eventChannelList) {
            channel.close()
        }
        for (job in websocketJobs) {
            job.cancel()
        }
    }

    @Throws(SerializationException::class)
    private suspend fun processFrameJson(
        frame: JsonElement,
        collector: MutableList<ByBitWebSocketMessage> = mutableListOf()
    ): List<ByBitWebSocketMessage> {

        if (frame is JsonArray) {
            frame.jsonArray.forEach { processFrameJson(it, collector) }
        }

        if (frame is JsonObject) {

            val topic = frame.jsonObject[TOPIC_MESSAGE_KEY]?.jsonPrimitive?.content

            val frameType = if (!topic.isNullOrBlank()) {
                topic.split('.').get(0)
            } else {
                frame.jsonObject[OPERATION_MESSAGE_KEY]?.jsonPrimitive?.content
            }
//            if (topic == "execution"){
//                println("\u001b[35m " + frameType.toString() + " \u001b[0m")
//            }

            val message = when (frameType) {
                "ping" -> {
                    val msg = serializer.decodeFromJsonElement(StatusMessage.serializer(), frame)
                    onPong(msg.reqId!!)
                    msg
                }
                "pong" -> { // this gets used in the "private" endpoint...API oddness
                    val msg = serializer.decodeFromJsonElement(StatusMessage.serializer(), frame)
                    onPong(msg.reqId!!)
                    msg
                }
                "auth" -> {
                    val msg = serializer.decodeFromJsonElement(StatusMessage.serializer(), frame)
                    if (msg.success!!) {
                        isAuthenticated.set(true)
                    } else {
                        if (msg.retMsg!!.contains("Repeat auth")) {
                            logger.warn { "T: Auth Repeated: ${msg.retMsg}" }
                        } else {
                            logger.error { "T: Auth Error: ${msg.retMsg}" }
                            exitProcess(1)
                        }
                    }
                    msg
                }

                "subscribe" -> {
                    serializer.decodeFromJsonElement(
                        StatusMessage.serializer(),
                        frame
                    )
                }

                "tickers" -> when (options.endpoint) {
                    ByBitEndpoint.Inverse, ByBitEndpoint.Linear -> serializer.decodeFromJsonElement(
                        TopicResponse.TickerLinearInverse.serializer(),
                        frame
                    )

                    ByBitEndpoint.Spot -> serializer.decodeFromJsonElement(TopicResponse.TickerSpot.serializer(), frame)
                    else -> {
                        RawMessage(frame.toString())
                    }
                }

                // same for all
                "publicTrade" -> serializer.decodeFromJsonElement(TopicResponse.PublicTrade.serializer(), frame)
                "kline" -> serializer.decodeFromJsonElement(TopicResponse.Kline.serializer(), frame)
                "liquidation" -> serializer.decodeFromJsonElement(TopicResponse.Liquidation.serializer(), frame)
                "orderbook" -> serializer.decodeFromJsonElement(TopicResponse.Orderbook.serializer(), frame)
                "execution" -> serializer.decodeFromJsonElement(PrivateTopicResponse.Execution.serializer(), frame)
                "order" -> serializer.decodeFromJsonElement(PrivateTopicResponse.Order.serializer(), frame)
                "wallet" -> serializer.decodeFromJsonElement(PrivateTopicResponse.Wallet.serializer(), frame)
                "position" -> serializer.decodeFromJsonElement(PrivateTopicResponse.Position.serializer(), frame)
                else -> RawMessage(frame.toString())
            }

            collector.add(message)
        }
        return collector
    }

    @Throws(SerializationException::class)
    private fun parseAuthenticationFrame(frame: Frame): Boolean {
        val response = serializer.parseToJsonElement(String(frame.readBytes()))

        if (response is JsonArray) {
            return response.any { parseStatusMessageForAuthenticationResult(it) }
        }

        if (response is JsonObject) {
            return parseStatusMessageForAuthenticationResult(response)
        }

        return false
    }

    @Throws(SerializationException::class)
    private fun parseStatusMessageForAuthenticationResult(message: JsonElement): Boolean {
        if (message.isAuthMessage()) {
            val status = serializer.decodeFromJsonElement(StatusMessage.serializer(), message)
            if (status.success == true) {
                isAuthenticated.set(true)
//                activeConnection?.isAuthenticated = true
//                listener.onAuthenticated(this)
                return true
            }
        }

        return false
    }

    /**
     * If the first byte in the message is an open square bracket, this frame is housing an array
     */
    private fun JsonElement.isAuthMessage() =
        this is JsonObject && JsonPrimitive(OPERATION_MESSAGE_KEY).contentOrNull == "auth"
}

/**
 * Convert a byte array to a hex string
 */
private fun ByteArray.toHex(): String {
    return this.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
}
