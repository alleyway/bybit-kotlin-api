package bybit.sdk.websocket

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
        @SerialName("T") val timestamp: Long? = null,
        @SerialName("s") val symbol: String? = null,
        @SerialName("S") val side: String? = null,
        @SerialName("v") val volume: Double? = null,
        @SerialName("p") val price: Double? = null,
    )

    @Serializable
    data class TickerItem(
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
        data class Ticker(
            val cs: Long,
            val data: TickerItem
        ) : TopicResponse()

        @Serializable
        data class Kline(
            @SerialName("data") val data: List<KlineItem>
        ) : TopicResponse()

    }


}



