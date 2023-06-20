package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5Paginatable
import bybit.sdk.rest.ListResult
import bybit.sdk.shared.OrderStatus
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable



suspend fun ByBitOrderClient.ordersOpen(
    params: OrdersOpenParams
): OrdersOpenResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "realtime",
        )
        params.category.let { parameters["category"] = it }
        params.symbol?.let { parameters["symbol"] = it }
        params.limit?.let { parameters["limit"] = it.toString() }
    }, HttpMethod.Get, false)


@Builder
data class OrdersOpenParams(
    val category: String, // 'spot' | 'linear' | 'inverse' | 'option'
    val symbol: String? = "",
    val limit: Int? = 50
    )

@Serializable
data class OrdersOpenResultItem(
    val orderId: String,
    val orderLinkId: String,
    val price: String,
    val qty: String,
    val orderStatus: OrderStatus
)

@Serializable
data class OrdersOpenListResult(
    override val category: String,
    override val list: List<OrdersOpenResultItem>,
    override val nextPageCursor: String? = ""
) : ListResult<OrdersOpenResultItem> {
}

@Serializable
data class OrdersOpenResponse(
    override val result: OrdersOpenListResult,
    override var nextUrl: String? = ""
) : APIResponseV5Paginatable<OrdersOpenResultItem>()


