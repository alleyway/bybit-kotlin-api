package bybit.sdk.sample

import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.account.WalletBalanceParams
import bybit.sdk.rest.okHttpClientProvider
import bybit.sdk.shared.AccountType
import bybit.sdk.websocket.*
import kotlinx.coroutines.delay
import kotlin.system.exitProcess


suspend fun main() {
    val bybitKey = System.getenv("BYBIT_API_KEY")
    val bybitSecret = System.getenv("BYBIT_SECRET")

    if (bybitKey.isNullOrEmpty() || bybitSecret.isNullOrEmpty()) {
        println("Make sure you set your BYBIT_API_KEY and BYBIT_SECRET environment variables!")
        exitProcess(1)
    }

    val bybitClient = ByBitRestClient(bybitKey, bybitSecret, true,  httpClientProvider = okHttpClientProvider)

    println("Blocking for wallet balance...")
    val walletBalanceResponse = bybitClient.accountClient.getWalletBalanceBlocking(WalletBalanceParams(AccountType.SPOT, listOf("BTC")))

    println("Got wallet balance synchronously: $walletBalanceResponse")

    println("\n\nWebsocket sample:")
    websocketSample(bybitKey, bybitSecret)

    exitProcess(0)

}

suspend fun websocketSample(bybitKey: String, bybitSecret: String) {

    val options = WSClientConfigurableOptions(ByBitEndpoint.Spot, bybitKey, bybitSecret, true)

    val websocketClient = ByBitWebSocketClient(
        options,
        object : ByBitWebSocketListener {
            override fun onAuthenticated(client: ByBitWebSocketClient) {
                println("Connected!")
            }

            override fun onReceive(
                client: ByBitWebSocketClient,
                message: ByBitWebSocketMessage
            ) {
                when (message) {
                    is ByBitWebSocketMessage.RawMessage -> println(message.data)
                    else -> println("Received Message: $message")
                }
            }

            override fun onDisconnect(client: ByBitWebSocketClient) {
                println("Disconnected!")
            }

            override fun onError(client: ByBitWebSocketClient, error: Throwable) {
                println("Error: ")
                error.printStackTrace()
            }

        })

    val subscriptions = listOf(
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Trades, "ETHUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Trades, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Liquidations, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Tickers, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Kline.Three_Minutes, "BTCUSD"),
//        ByBitWebSocketSubscription(ByBitWebsocketTopic.Liquidations, "BTCUSD"),
        ByBitWebSocketSubscription(ByBitWebsocketTopic.Orderbook.Level_500, "BTCUSD")
    )

    websocketClient.connect()
    websocketClient.subscribe(subscriptions)
    delay(15000)
    websocketClient.unsubscribe(subscriptions)
    websocketClient.disconnect()
}

