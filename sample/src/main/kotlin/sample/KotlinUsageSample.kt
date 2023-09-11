package sample

import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.account.WalletBalanceParams
import bybit.sdk.rest.okHttpClientProvider
import bybit.sdk.shared.AccountType
import bybit.sdk.websocket.*
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

    val options = WSClientConfigurableOptions(ByBitEndpoint.Inverse, bybitKey, bybitSecret, true)

    val websocketClient = ByBitWebSocketClient(options)

    val subscriptions = listOf(
        ByBitWebSocketSubscription(ByBitWebsocketTopic.Orderbook.Level_500, "BTCUSD")
    )


    websocketClient.connect(subscriptions)

    val startTime = System.currentTimeMillis()
    val duration = 10 * 1000 // 10 seconds in milliseconds

    val channel = websocketClient.getWebSocketEventChannel()
    while (true) {
        // Your loop code here
        val msg = channel.receive()

        println(msg.toString())

        // Check if 10 seconds have elapsed
        if (System.currentTimeMillis() - startTime >= duration) {
            break // Exit the loop after 10 seconds
        }
    }

    websocketClient.unsubscribe(subscriptions)
    websocketClient.disconnect()
}

