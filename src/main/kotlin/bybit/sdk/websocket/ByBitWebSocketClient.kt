package bybit.sdk.websocket

import bybit.sdk.DefaultJvmHttpClientProvider
import bybit.sdk.HttpClientProvider
import bybit.sdk.Version
import bybit.sdk.ext.ByBitCompletionCallback
import bybit.sdk.ext.coroutineToCompletionCallback
import bybit.sdk.shared.sha256_HMAC
import bybit.sdk.websocket.ByBitWebSocketMessage.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.time.Instant
import java.util.*

private const val TOPIC_MESSAGE_KEY = "topic"
private const val OPERATION_MESSAGE_KEY = "op"

/*

Spot: wss://stream.bybit.com/v5/public/spot
USDT and USDC perpetual: wss://stream.bybit.com/v5/public/linear
Inverse contract: wss://stream.bybit.com/v5/public/inverse
USDC Option: wss://stream.bybit.com/v5/public/option

 */


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
    val pingInterval: Long = 10000,
) {
    constructor(endpoint: ByBitEndpoint, testnet: Boolean) : this(endpoint, null, null, testnet) {

    }
}

//    /**
//     * The API group this client should connect to.
//     *
//     * For the V3 APIs use `v3` as the market (spot/unified margin/usdc/account asset/copy trading)
//     */
//    market: APIMarket;
//
//    pongTimeout?: number;
//    pingInterval?: number;
//    reconnectTimeout?: number;
//    restOptions?: RestClientOptions;
//    // eslint-disable-next-line @typescript-eslint/no-explicit-any
//    requestOptions?: any;
//    wsUrl?: string;
//    /** If true, fetch server time before trying to authenticate (disabled by default) */
//    fetchTimeOffsetBeforeAuth?: boolean;
//}

/**
 *
 * @param apiKey the API key to use with all API requests
 * @param secret the API secret to use with all API requests
 * @param endpoint the [ByBitEndpoint] to connect to
 * @param listener the [ByBitWebSocketListener] to send events to
 * @param bufferSize the size of the back buffer to use when websocket events start coming in faster than they can be processed. To drop all but the latest event, use [Channel.CONFLATED]
 * @param httpClientProvider (Optional) A provider for the ktor [HttpClient] to use; defaults to [DefaultJvmHttpClientProvider]
 * @param bybitWebSocketDomain (Optional) The domain to connect to for all websockets; defaults to ByBit's websocket domain "stream-testnet.bybit.com". Useful for overriding in a testing environment
 * @param coroutineScope The coroutine scope to launch the web socket processing in. For info on coroutines, see here: https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html
 */
class ByBitWebSocketClient
@JvmOverloads
constructor(
    val options: WSClientConfigurableOptions,
    private val listener: ByBitWebSocketListener,
    private val bufferSize: Int = Channel.UNLIMITED,
    private val httpClientProvider: HttpClientProvider = DefaultJvmHttpClientProvider(),
    timer: Timer = Timer()

) {

    val task = object : TimerTask() {
        override fun run() {
            sendPingAsync(null)
        }
    }

    init {
        timer.scheduleAtFixedRate(task, 20000L, options.pingInterval)
    }

    private val serializer by lazy {
        Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    private var activeConnection: WebSocketConnection? = null

    /**
     * Set this to true if you want to parse incoming messages yourself
     */
    var sendRaw: Boolean = false

    /**
     * The coroutine scope to launch the web socket processing in.
     * This should be set before connecting to the websocket
     * For info on coroutines, see here: https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html
     */
    var coroutineScope: CoroutineScope = GlobalScope


    /**
     * Connect and authenticate to the given [ByBitEndpoint].
     *
     * Calling from java? see [connectBlocking] and [connectAsync]
     */
    suspend fun connect() {
        if (activeConnection != null) {
            return
        }

        val client = httpClientProvider.buildClient()
        val session = client.webSocketSession {
            host = if (options.testnet) {
                "stream-testnet.bybit.com"
            } else {
                "stream.bybit.com"
            }

            url.protocol = URLProtocol.WSS
            url.port = URLProtocol.WSS.defaultPort
            url.path(*options.endpoint.pathComponents)

            headers["User-Agent"] = Version.userAgent
        }

        activeConnection = WebSocketConnection(client, session)

        try {
            session.incoming
                .consumeAsFlow()
                .buffer(bufferSize)
                .onEach(this::processFrame)
                .onCompletion { listener.onDisconnect(this@ByBitWebSocketClient) }
                .launchIn(coroutineScope)
        } catch (ex: Exception) {
            listener.onError(this, ex)
        }
        sendPing()
    }

    /** Blocking version of [connect] */
    fun connectBlocking() = runBlocking { connect() }

    /** Async/callback version of [connect] */
    fun connectAsync(callback: ByBitCompletionCallback?) =
        coroutineToCompletionCallback(callback, coroutineScope) { connect() }

    suspend fun sendPing() {
        activeConnection?.webSocketSession?.send("""{"req_id": "100001", "op": "ping"}""")
    }

    /** Blocking version of [sendPing] */
    fun sendPingBlocking() = runBlocking { sendPing() }

    /** Async/callback version of [sendPing] */
    fun sendPingAsync(callback: ByBitCompletionCallback?) =
        coroutineToCompletionCallback(callback, coroutineScope) { sendPing() }


    /**
     * Subscribe to one or more data streams. Be sure to subscribe to streams available in the
     * current cluster.
     *
     * Calling from Java? See [subscribeBlocking] and [subscribeAsync]
     */
    suspend fun subscribe(subscriptions: List<ByBitWebSocketSubscription>) {
        if (subscriptions.isEmpty()) return

        if (
            subscriptions.map { it.topic }.filterIsInstance<ByBitWebsocketTopic.PrivateTopic>().size > 0
            && activeConnection?.isAuthenticated != true
        ) {

            val apiKey = options.key
            val expires = Instant.now().toEpochMilli() + 10000

            val toEncode = "GET/realtime$expires"


            val signature = sha256_HMAC(toEncode, options.secret)

            activeConnection
                ?.webSocketSession
                ?.send(
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

            Thread.sleep(1000)
        }

        activeConnection
            ?.webSocketSession
            ?.send("""{"op": "subscribe", "args":["${subscriptions.joinToString(separator = "\",\"")}"]}""")
    }

    /** Blocking version of [subscribe]  */
    fun subscribeBlocking(subscriptions: List<ByBitWebSocketSubscription>) =
        runBlocking { subscribe(subscriptions) }

    /** Async/callback version of [subscribe] */
    fun subscribeAsync(subscriptions: List<ByBitWebSocketSubscription>, callback: ByBitCompletionCallback?) =
        coroutineToCompletionCallback(callback, coroutineScope) { subscribe(subscriptions) }

    /**
     * Unsubscribe to one or more data streams
     *
     * Calling from Java? See [unsubscribeBlocking] and [unsubscribeAsync]
     */
    suspend fun unsubscribe(subscriptions: List<ByBitWebSocketSubscription>) {
        if (subscriptions.isEmpty()) return

        activeConnection
            ?.webSocketSession
            ?.send("""{"op": "unsubscribe", "args":["${subscriptions.joinToString(separator = "\",\"")}"]}""")
    }

    /** Blocking version of [unsubscribe] */
    fun unsubscribeBlocking(subscriptions: List<ByBitWebSocketSubscription>) =
        runBlocking { unsubscribe(subscriptions) }

    /** Callback/async version of [unsubscribe] */
    fun unsubscribeAsync(subsciptions: List<ByBitWebSocketSubscription>, callback: ByBitCompletionCallback?) =
        coroutineToCompletionCallback(callback, coroutineScope) { unsubscribe(subsciptions) }

    /**
     * Disconnect this client from the websocket server.
     *
     * Calling from Java? See [disconnectBlocking] and [disconnectAsync]
     */
    suspend fun disconnect() {
        activeConnection?.disconnect()
        activeConnection = null

        listener.onDisconnect(this)
    }

    /** Blocking version of [disconnect] */
    fun disconnectBlocking() = runBlocking { disconnect() }

    /** Async/callback version of [disconnect] */
    fun disconnectAsync(callback: ByBitCompletionCallback?) =
        coroutineToCompletionCallback(callback, coroutineScope) { disconnect() }

    private suspend fun processFrame(frame: Frame) {
        try {
            if (activeConnection?.isAuthenticated == false) {
                if (parseAuthenticationFrame(frame)) {
                    return
                }
            }

            if (sendRaw) {
                listener.onReceive(this, RawMessage(frame.toString()))
            } else {
                val json = serializer.parseToJsonElement(String(frame.readBytes()))
                processFrameJson(json).forEach { listener.onReceive(this, it) }
            }
        } catch (ex: Exception) {
            listener.onReceive(this, RawMessage(frame.toString()))
            listener.onError(this, ex)
        }
    }

    @Throws(SerializationException::class)
    private fun processFrameJson(
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
                "pong", "ping", "subscribe", "auth" -> serializer.decodeFromJsonElement(
                    StatusMessage.serializer(),
                    frame
                )

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
                activeConnection?.isAuthenticated = true
                listener.onAuthenticated(this)
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

private class WebSocketConnection(
    val httpClient: HttpClient,
    val webSocketSession: WebSocketSession,
    var isAuthenticated: Boolean = false
) {

    suspend fun disconnect() {
        webSocketSession.close()
        httpClient.close()
    }
}
