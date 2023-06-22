package bybit.sdk.websocket

data class ByBitWebSocketSubscription(
    val topic: ByBitWebsocketTopic,
    val symbol: String = ""
) {
    override fun toString() =
        if (topic.extra.isNotBlank()) {
        "${topic.prefix}.${topic.extra}.$symbol"
    } else {
        if (symbol.isNotBlank()) {
            "${topic.prefix}.$symbol"
        } else {
            topic.prefix
        }
    }
}

sealed class ByBitWebsocketTopic(val prefix: String, val extra: String = "") {

    sealed class Orderbook(depth: String) : ByBitWebsocketTopic("orderbook", depth) {
        object Level_1 : Orderbook("1")
        object Level_25 : Orderbook("25")
        object Level_50 : Orderbook("50")
        object Level_100 : Orderbook("100")
        object Level_200 : Orderbook("200")
        object Level_500 : Orderbook("500")
    }

    object Trades : ByBitWebsocketTopic("publicTrade")

    object Tickers : ByBitWebsocketTopic("tickers")

    sealed class Kline(interval: String) : ByBitWebsocketTopic("kline", interval) {
        object One_Minute : Kline("1")
        object Three_Minutes : Kline("3")
        object Five_Minutes : Kline("5")
        object Fifteen_Minutes : Kline("15")
        object Half_Hourly : Kline("30")
        object Hourly : Kline("60")
        object Two_Hourly : Kline("120")
        object Four_Hourly : Kline("240")
        object Six_Hourly : Kline("360")
        object Twelve_Hourly : Kline("720")
        object Daily : Kline("D")
        object Weekly : Kline("W")
        object Monthly : Kline("M")
    }


    sealed class PrivateTopic(prefix: String) : ByBitWebsocketTopic(prefix) {
        object Execution : PrivateTopic("execution")
        object Order : PrivateTopic("order")
    }

    object Liquidations : ByBitWebsocketTopic("liquidation")


    /**
     * Use this if there's a new channel that this SDK doesn't fully support yet
     */
    class Other(topicPrefix: String) : ByBitWebsocketTopic(topicPrefix)
}
