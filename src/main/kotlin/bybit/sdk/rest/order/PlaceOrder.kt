package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable


@SafeVarargs
suspend fun ByBitOrderClient.placeOrder(
    params: PlaceOrderParams
): PlaceOrderResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "create",
        )
        params.category.let { parameters["category"] = it }
        params.symbol.let { parameters["symbol"] = it }
        params.side.let { parameters["side"] = it.value }
        params.orderType.let { parameters["orderType"] = it.value }
        params.qty.let { parameters["qty"] = it }
        params.price?.let { parameters["price"] = it }
        params.orderLinkId?.let { parameters["orderLinkId"] = it }
    }, HttpMethod.Post, false)


@Builder
data class PlaceOrderParams(
    val category: String, // 'spot' | 'linear' | 'inverse' | 'option'
    val symbol: String,
    val side: Side,
    val orderType: OrderType = OrderType.Market,
    val qty: String,
    val price: String? = null,
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

