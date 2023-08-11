package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable



// this doesn't work for spot!
suspend fun ByBitOrderClient.amendOrder(
    params: AmendOrderParams
): AmendOrderResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "amend",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.qty.let { parameters["qty"] = it }
        params.price?.let { parameters["price"] = it }
        params.orderLinkId?.let { parameters["orderLinkId"] = it }
    }, HttpMethod.Post, false)


@Builder
data class AmendOrderParams(
    val category: Category,
    val symbol: String,
    val orderId: String? = null,
    val orderLinkId: String? = null,
    val qty: String,
    val price: String? = null
)

@Serializable
data class AmendOrderResult(
    val orderId: String,
    val orderLinkId: String
)


@Serializable
data class AmendOrderResponse(
    val result: AmendOrderResult
) : APIResponseV5()


