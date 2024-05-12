package bybit.sdk.websocket

import bybit.sdk.properties.ByBitProperties
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import org.junit.jupiter.api.Test

internal class WebSocketTest {


    val bybitKey = System.getenv("BYBIT_API_KEY") ?: ByBitProperties.APIKEY
    val bybitSecret = System.getenv("BYBIT_SECRET") ?: ByBitProperties.SECRET

    val privateSubs = listOf(
        ByBitWebSocketSubscription(ByBitWebsocketTopic.PrivateTopic.Execution),
        ByBitWebSocketSubscription(ByBitWebsocketTopic.PrivateTopic.Order),
        ByBitWebSocketSubscription(ByBitWebsocketTopic.PrivateTopic.Wallet)
    )

    @Test
    fun publicWebsocketTest() {

        val wsClientOne = ByBitWebSocketClient(
            options = WSClientConfigurableOptions(
                endpoint = ByBitEndpoint.Inverse,
                key = bybitKey,
                secret = bybitSecret,
                testnet = true,
            ),
        )

//        val wsClientTwo = ByBitWebSocketClient(
//            options = WSClientConfigurableOptions(
//                endpoint = ByBitEndpoint.Private,
//                key = bybitKey,
//                secret = bybitSecret,
//                testnet = true,
//            ),
//        )

        runBlocking {

            val scopeOne = CoroutineScope(Dispatchers.Default + Job())
            scopeOne.launch {
                wsClientOne.connect(listOf(ByBitWebSocketSubscription(ByBitWebsocketTopic.Trades, "BTCUSD")))

                val channel = wsClientOne.getWebSocketEventChannel(10, BufferOverflow.SUSPEND)

                while (true) {
                    val msg = channel.receive()
                    println(msg)
//                    (this@::handler)(msg)
                }
            }

//            val scopeTwo = CoroutineScope(Dispatchers.Default + Job())
//
//
//            scopeTwo.launch {
//                wsClientTwo.connect(privateSubs)
//                val channel = wsClientOne.getWebSocketEventChannel()
//                while (true) {
//                    val msg = channel.receive()
//                    println(msg)
////                    (this@::handler)(msg)
//                }
//            }

            delay(30_000)
        }

    }

    @Test
    fun privateWebsocketTest() {
        val wsClientPrivate = ByBitWebSocketClient(
            options = WSClientConfigurableOptions(
                endpoint = ByBitEndpoint.Private,
                key = bybitKey,
                secret = bybitSecret,
                testnet = true,
            ),
//        httpClientProvider = okHttpClientProvider
        )

        runBlocking {
            val scopeOne = CoroutineScope(Dispatchers.Default + Job())
            scopeOne.launch {

                wsClientPrivate.connect(privateSubs)

                val channel = wsClientPrivate.getWebSocketEventChannel(10, BufferOverflow.SUSPEND)
                while (true) {
                    when (val message = channel.receive()) {
                        is ByBitWebSocketMessage.StatusMessage -> {
                            message.success?.let {
                                if (!it) {
                                    println("Error: " + message)
                                }
                            }
                        }

                        else -> {
                            println(message.toString())
                        }
                    }
                }
            }
            delay(30_000)
        }
    }
}
