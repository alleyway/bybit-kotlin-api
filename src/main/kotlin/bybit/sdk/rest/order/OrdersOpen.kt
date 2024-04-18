package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5Paginatable
import bybit.sdk.rest.ListResult
import bybit.sdk.shared.Category
import bybit.sdk.shared.OrderStatus
import bybit.sdk.shared.OrderType
import bybit.sdk.shared.Side
import bybit.sdk.shared.TimeInForce
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder


suspend fun ByBitOrderClient.ordersOpen(
    params: OrdersOpenParams
): OrdersOpenResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "realtime",
        )
        parameters["category"] = params.category.toString()
        params.symbol?.let { parameters["symbol"] = it }
        params.baseCoin?.let { parameters["baseCoin"] = it }
        params.orderId?.let { parameters["orderId"] = it }
        params.orderLinkId?.let { parameters["orderLinkId"] = it }
        params.openOnly?.let { parameters["openOnly"] = it.toString() }
        params.orderFilter?.let { parameters["orderFilter"] = it }
        params.limit?.let { parameters["limit"] = it.toString() }
    }, HttpMethod.Get, false)


@Builder
data class OrdersOpenParams(
    val category: Category,
    val symbol: String? = null,
    val baseCoin: String? = null,
    val settleCoin: String? = null,
    val orderId: String? = null,
    val orderLinkId: String? = null,
    val openOnly: Int? = null,
    val orderFilter: String? = null,
    val limit: Int? = 50
)

@Serializable
data class OrdersOpenResultItem(
    val orderId: String,
    val orderLinkId: String,
    val blockTradeId: String,
    val symbol: String,
    val price: String,
    val qty: String,
    val side: Side,
    val isLeverage: String,
    val positionIdx: Int,
    val orderStatus: OrderStatus,
    val createType: String,
    val cancelType: String,
    val rejectReason: String,
    val avgPrice: String,
    val leavesQty: String,
    val leavesValue: String,
    val cumExecQty: String,
    val cumExecValue: String,
    val cumExecFee: String,
    val timeInForce: TimeInForce,
    val orderType: OrderType,
    val stopOrderType: String,
    val orderIv: String,
    val triggerPrice: String,
    val takeProfit: String,
    val stopLoss: String,
    val tpTriggerBy: String,
    val slTriggerBy: String,
    val triggerDirection: Int,
    val triggerBy: String,
    val lastPriceOnCreated: String,
    val reduceOnly: Boolean,
    val closeOnTrigger: Boolean,
    val smpType: String,
    val smpGroup: Int,
    val smpOrderId: String,
    val tpslMode: String,
    val tpLimitPrice: String,
    val slLimitPrice: String,
    val placeType: String,
    val createdTime: Long,
    val updatedTime: Long
)

@Serializable
data class OrdersOpenListResult(
    override val category: Category,
    override val list: List<OrdersOpenResultItem>,
    override val nextPageCursor: String? = ""
) : ListResult<OrdersOpenResultItem> {
}

@Serializable
data class OrdersOpenResponse(
    override val result: OrdersOpenListResult,
    override var nextUrl: String? = ""
) : APIResponseV5Paginatable<OrdersOpenResultItem>()


