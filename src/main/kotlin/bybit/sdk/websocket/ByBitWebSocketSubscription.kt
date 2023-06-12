package bybit.sdk.websocket

data class ByBitWebSocketSubscription(
    val channel: ByBitWebSocketChannel,
    val symbol: String
) {
    override fun toString() = if (channel.extra.isNotBlank()) {
        "${channel.prefix}.${channel.extra}.$symbol"
    } else {
        "${channel.prefix}.$symbol"
    }
}

sealed class ByBitWebSocketChannel(val prefix: String, val extra: String = "") {

    sealed class Shared(channelPrefix: String, extra: String) : ByBitWebSocketChannel(channelPrefix, extra) {
        sealed class Kline(interval: String) : Shared("kline", interval) {
            object One_Minute: Kline("1")
            object Three_Minutes: Kline("3")
            object Five_Minutes: Kline("5")
            object Fifteen_Minutes: Kline("15")
            object Half_Hourly: Kline("30")
            object Hourly: Kline("60")
            object Two_Hourly: Kline("120")
            object Four_Hourly: Kline("240")
            object Six_Hourly: Kline("360")
            object Twelve_Hourly: Kline("720")
            object Daily: Kline("D")
            object Weekly: Kline("W")
            object Monthly: Kline("M")
        }
    }

    sealed class Contract(channelPrefix: String) : ByBitWebSocketChannel(channelPrefix) {
        object Trades : Contract("publicTrade")
        object Tickers : Contract("tickers")
        object Liquidations : Contract("liquidation")
    }

    /**
     * Use this if there's a new channel that this SDK doesn't fully support yet
     */
    class Other(channelPrefix: String) : ByBitWebSocketChannel(channelPrefix)
}
