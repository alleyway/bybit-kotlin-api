package bybit.sdk.websocket

data class ByBitWebSocketSubscription(
	val channel: ByBitWebSocketChannel,
	val symbol: String
) {
    override fun toString() = "${channel.prefix}.$symbol"
}

sealed class ByBitWebSocketChannel(val prefix: String) {

    sealed class Contract(channelPrefix: String) : ByBitWebSocketChannel(channelPrefix) {
        object Trades: Contract("dummy")
//        object Quotes: Stocks("Q")
    }



    /**
     * Use this if there's a new channel that this SDK doesn't fully support yet
     */
    class Other(channelPrefix: String) : ByBitWebSocketChannel(channelPrefix)
}