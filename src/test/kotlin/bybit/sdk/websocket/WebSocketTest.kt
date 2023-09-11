package bybit.sdk.websocket

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

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

    val privateSubscriptions = listOf(
        ByBitWebSocketSubscription(ByBitWebsocketTopic.PrivateTopic.Order),
    )

    @Test
    fun publicWebsocketTest() {
        val wsClient = ByBitWebSocketClient(
            options = WSClientConfigurableOptions(
                endpoint = ByBitEndpoint.Inverse,
                key = bybitKey,
                secret = bybitSecret,
                testnet = true,
            ),
//        httpClientProvider = okHttpClientProvider
        )
        runBlocking {
            wsClient.connect(publicSubscriptions)

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
            wsClient.connect(privateSubscriptions)

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
