package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable


suspend fun ByBitOrderClient.cancelOrder(
    params: CancelOrderParams
): CancelOrderResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "cancel",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.orderId?.let { parameters["orderId"] = it }
        params.orderLinkId?.let { parameters["orderLinkId"] = it }
    }, HttpMethod.Post, false)


@Builder
data class CancelOrderParams(
    val category: Category,
    val symbol: String,
    val orderId: String? = null,
    val orderLinkId: String? = null
)

@Serializable
data class CancelOrderResult(
    val orderId: String,
    val orderLinkId: String
)


@Serializable
data class CancelOrderResponse(
    val result: CancelOrderResult
) : APIResponseV5()


