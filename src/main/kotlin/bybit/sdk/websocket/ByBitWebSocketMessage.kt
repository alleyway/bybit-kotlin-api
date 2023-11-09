package bybit.sdk.websocket

import bybit.sdk.shared.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Messages that ByBit web sockets might return.
 */


sealed class ByBitWebSocketMessage {

    data class RawMessage(val data: String) : ByBitWebSocketMessage()

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
        @SerialName("S") val side: Side,
        @SerialName("v") val volume: Double,
        @SerialName("p") val price: Double,
        @SerialName("L") val tickDirection: TickDirection,
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
        val side: Side,
        val size: Double,
        val symbol: String,
        val updatedTime: Long,
    )

    @Serializable
    data class OrderBookItem(
        @SerialName("s") val symbol: String? = null,
        @SerialName("b") val bids: List<List<Double>> = emptyList(), // [0] is asking price, [1] is asking size
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
        val isLeverage: String,
        val orderId: String,
        val orderLinkId: String,
        val side: Side,
        val orderPrice: String,
        val orderQty: String,
        val leavesQty: String,
        val orderType: OrderType,
        val execFee: String,
        val execId: String,
        val execPrice: String,
        val execQty: String,
        val execType: ExecType?,
        val execValue: String?,
        val execTime: String,
        val isMaker: Boolean,
        val feeRate: String,
        val closedSize: String
    )


    @Serializable
    data class CoinItem(
        val coin: String,
        val equity: String,
        val usdValue: String,
        val walletBalance: String,
        val free: String? = null, // only for Classic SPOT
        val locked: String? = null,
        val borrowAmount: String,
        val availableToBorrow: String,
        val availableToWithdraw: String,
        val accruedInterest: String,
        val totalOrderIM: String,
        val totalPositionIM: String,
        val totalPositionMM: String,
        val unrealisedPnl: String,
        val cumRealisedPnl: String,
        val bonus: String? = null, // unique field for UNIFIED account
        val collateralSwitch: Boolean? = null,
        val marginCollateral: Boolean? = null
    )

    @Serializable
    data class WalletItem(
        val accountIMRate: String,
        val accountMMRate: String,
        val totalEquity: String,
        val totalWalletBalance: String,
        val totalMarginBalance: String,
        val totalAvailableBalance: String,
        val totalPerpUPL: String,
        val totalInitialMargin: String,
        val accountType: AccountType,
        val accountLTV: String,
        val coin: List<CoinItem>
    )

    @Serializable
    data class OrderItem(
        val category: Category,
        val symbol: String,
        val orderId: String,
        val side: Side,
        val orderType: OrderType,
        val cancelType: String,
        val price: String,
        val qty: String,
        val orderIv: String,
        val timeInForce: String,
        val orderStatus: OrderStatus,
        val orderLinkId: String,
        val lastPriceOnCreated: String,
        val reduceOnly: Boolean,
        val leavesQty: String,
        val leavesValue: String,
        val cumExecQty: String,
        val cumExecValue: String,
        val avgPrice: String,
        val blockTradeId: String,
        val positionIdx: Int,
        val cumExecFee: String,
        val createdTime: String,
        val updatedTime: String,
        val rejectReason: String,
        val stopOrderType: String,
        val tpslMode: String = "",
        val triggerPrice: String,
        val takeProfit: String,
        val stopLoss: String,
        val tpTriggerBy: String,
        val slTriggerBy: String,
        val tpLimitPrice: String = "",
        val slLimitPrice: String = "",
        val triggerDirection: Int,
        val triggerBy: String,
        val closeOnTrigger: Boolean,
        val placeType: String = "",
        val smpType: String,
        val smpGroup: Int,
        val smpOrderId: String
    )

    @Serializable
    sealed class PrivateTopicResponse : ByBitWebSocketMessage() {

        val id: String? = null

        val topic: String? = null

        val creationTime: Long? = null

        @Serializable
        data class Execution(
            val data: List<ExecutionItem>
        ) : PrivateTopicResponse()

        @Serializable
        data class Order(
            val data: List<OrderItem>
        ) : PrivateTopicResponse()

        @Serializable
        data class Wallet(
            val data: List<WalletItem>
        ) : PrivateTopicResponse()
    }

}



