package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import bybit.sdk.shared.OrderType
import bybit.sdk.shared.Side
import bybit.sdk.shared.TimeInForce
import lombok.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable


suspend fun ByBitOrderClient.placeOrder(
    params: PlaceOrderParams
): PlaceOrderResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "create",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.side.let { parameters["side"] = it.toString() }
        params.orderType.let { parameters["orderType"] = it.toString() }
        params.qty.let { parameters["qty"] = it }
        params.price?.let { parameters["price"] = it }
        params.timeInForce?.let { parameters["timeInForce"] = it.toString() }
        params.orderLinkId?.let { parameters["orderLinkId"] = it }
        params.reduceOnly?.let { parameters["reduceOnly"] = it.toString() }
    }, HttpMethod.Post, false)


@Builder
data class PlaceOrderParams(
    val category: Category,
    val symbol: String,
    val side: Side,
    val orderType: OrderType = OrderType.Market,
    val qty: String,
    val reduceOnly: Boolean? = null,
    val price: String? = null,
    val timeInForce: TimeInForce? = null,
    val orderLinkId: String? = null
)

@Serializable
data class PlaceOrderResult(
    val orderId: String,
    val orderLinkId: String
)


@Serializable
data class PlaceOrderResponse(
    val result: PlaceOrderResult
) : APIResponseV5()


