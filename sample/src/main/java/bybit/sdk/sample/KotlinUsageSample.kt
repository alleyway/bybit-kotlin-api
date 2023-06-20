package bybit.sdk.sample

import bybit.sdk.websocket.*
import kotlinx.coroutines.delay
import kotlin.system.exitProcess


suspend fun main() {
    val bybitKey = System.getenv("BYBIT_API_KEY")
    val bybitSecret = System.getenv("BYBIT_SECRET")

    if (bybitKey.isNullOrEmpty() || bybitSecret.isNullOrEmpty()) {
        println("Make sure you set your BYBIT_API_KEY andBYBIT_SECRET environment variable!")
        exitProcess(1)
    }

//    val bybitClient = ByBitRestClient(bybitKey, bybitSecret,  httpClientProvider = okHttpClientProvider)
//
//    println("Blocking for server time...")
//    val serverTimeResponse = bybitClient.contractClient.getServerTimeBlocking()
//    println("Got server time synchronously: $serverTimeResponse")

//    println("Getting server time asynchronously...")
//    val deferred = GlobalScope.async {
//        val asyncTime = bybitClient.contractClient.getServerTime()
//        println("Got server time asynchronously: $asyncTime")
//    }

    // deferred.await()
//    println("Done getting time asynchronously!")
//
//    println("Using options")
//    val groupedDaily = bybitClient.getGroupedDailyAggregates(
//        GroupedDailyParameters("us", "stocks", "2022-12-08"),
//        ByBitRestOptions.withTimeout(10_000), // Custom timeout for this request
//        ByBitRestOptions.withQueryParam("additional-param", "additional-value"), // Additional query parameter
//        ByBitRestOptions.withHeader("X-Custom-Header", "custom-value"), // Custom header for this request
//        { this.expectSuccess = true }, // Example of an arbitrary option that doesn't use a helper function
//    )
//
//    println("Got ${groupedDaily.results.size} results from grouped daily")
//
//    iteratorExample(bybitClient)
//    tradesIteratorExample(bybitClient)
//    quotesIteratorExample(bybitClient)
//
//    financialsSample(bybitClient)
//
//    indicesSample(bybitClient)
//
//    println("\n\nWebsocket sample:")
    websocketSample(bybitKey, bybitSecret)

    exitProcess(0)

}

suspend fun websocketSample(bybitKey: String, bybitSecret: String) {

    val options = WSClientConfigurableOptions(bybitKey,bybitSecret,true)

    val websocketClient = ByBitWebSocketClient(
        ByBitEndpoint.Inverse,
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
                    is ByBitWebSocketMessage.RawMessage -> println(String(message.data))
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

