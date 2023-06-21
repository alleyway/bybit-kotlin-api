package bybit.sdk.websocket

import bybit.sdk.shared.Category
import bybit.sdk.shared.Side
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Messages that ByBit web sockets might return.
 */


sealed class ByBitWebSocketMessage {

    data class RawMessage(val data: ByteArray) : ByBitWebSocketMessage()

    @Serializable
    data class StatusMessage(
        val op: String? = null,
        @SerialName("req_id") val reqId: String? = null,
        @SerialName("ret_msg") val retMsg: String? = null,
        @SerialName("conn_id") val connId: String? = null,
        val success: Boolean? = null
    ) : ByBitWebSocketMessage()

    @Serializable
    data class PublicTradeItem(
        @SerialName("T") val timestamp: Long,
        @SerialName("s") val symbol: String,
        @SerialName("S") val side: String,
        @SerialName("v") val volume: Double,
        @SerialName("p") val price: Double,
    )

    @Serializable
    data class TickerLinearInverseItem(
        val symbol: String? = null,
        val price24hPcnt: Double? = null,
        val fundingRate: Double? = null,
        val markPrice: Double? = null,
        val indexPrice: Double? = null,
        val openInterestValue: Double? = null,
        val bid1Price: Double? = null,
        val bid1Size: Double? = null,
        val ask1Price: Double? = null,
        val ask1Size: Double? = null,
    )

    @Serializable
    data class TickerSpotItem(
        val symbol: String? = null,
        val lastPrice: Double? = null,
        val highPrice24h: Double? = null,
        val lowPrice24h: Double? = null,
        val prevPrice24h: Double? = null,
        val volume24h: Double? = null,
        val turnover24h: Double? = null,
        val price24hPcnt: Double? = null,
        val usdIndexPrice: String? = null,
    )

    @Serializable
    data class KlineItem(
        val start: Long,
        val end: Long,
        val interval: String,
        val open: Double,
        val close: Double,
        val high: Double,
        val low: Double,
        val volume: Double,
        val turnover: Double,
        val confirm: Boolean,
        val timestamp: Long,
    )

    @Serializable
    data class LiquidationItem(
        val price: Double,
        val side: String,
        val size: Double,
        val symbol: String,
        val updatedTime: Long,
    )

    @Serializable
    data class OrderBookItem(
        @SerialName("s") val symbol: String? = null,
        @SerialName("b") val bids: List<List<Double>> = emptyList(),
        @SerialName("a") val asks: List<List<Double>> = emptyList(),
        @SerialName("u") val updateId: Int,
    )

    @Serializable
    sealed class TopicResponse : ByBitWebSocketMessage() {

        @SerialName("topic")
        val topic: String? = null

        @SerialName("type")
        val type: String? = null

        @SerialName("ts")
        val ts: Long? = null

        @Serializable
        data class PublicTrade(
            @SerialName("data") val data: List<PublicTradeItem>
        ) : TopicResponse()

        @Serializable
        data class TickerLinearInverse(
            val cs: Long,
            val data: TickerLinearInverseItem
        ) : TopicResponse()

        @Serializable
        data class TickerSpot(
            val cs: Long,
            val data: TickerSpotItem
        ) : TopicResponse()

        @Serializable
        data class Kline(
            @SerialName("data") val data: List<KlineItem>
        ) : TopicResponse()

        @Serializable
        data class Liquidation(
            val data: LiquidationItem
        ) : TopicResponse()

        @Serializable
        data class Orderbook(
            val data: OrderBookItem
        ) : TopicResponse()

    }



    // Private


    @Serializable
    data class ExecutionItem(
        val category: Category,
        val symbol: String,
        val side: Side,
        val execQty: String,
        val execPrice: String,
        val execTime: String
    )


    @Serializable
    sealed class PrivateTopicResponse : ByBitWebSocketMessage() {

        val id: String? = null

        val topic: String? = null

        val creationTime: Long? = null

        @Serializable
        data class Execution(
            @SerialName("data") val data: List<ExecutionItem>
        ) : PrivateTopicResponse()
    }

}



