package bybit.sdk.websocket

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

internal class WebSocketTest {

    val bybitKey: String
    val bybitSecret: String

    init {
        bybitKey = System.getenv("BYBIT_API_KEY")
        bybitSecret = System.getenv("BYBIT_SECRET")
    }

    val publicSubscriptions = listOf(
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Trades, "ETHUSD"),
        ByBitWebSocketSubscription(ByBitWebsocketTopic.Trades, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.PrivateTopic.Order, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Liquidations, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Tickers, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Liquidations, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Orderbook.Level_500, "BTCUSD")
    )

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

                val channel = wsClientOne.getWebSocketEventChannel()

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
        val wsClient = ByBitWebSocketClient(
            options = WSClientConfigurableOptions(
                endpoint = ByBitEndpoint.Private,
                key = bybitKey,
                secret = bybitSecret,
                testnet = true,
            ),
//        httpClientProvider = okHttpClientProvider
        )
        runBlocking {
            wsClient.connect(privateSubs)

            val channel = wsClient.getWebSocketEventChannel()

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
    }
}
