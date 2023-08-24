package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5Paginatable
import bybit.sdk.rest.ListResult
import bybit.sdk.shared.Category
import lombok.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable



suspend fun ByBitOrderClient.orderHistory(
    params: OrderHistoryParams
): OrderHistoryResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "history",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it }
        params.limit?.let { parameters["limit"] = it.toString() }
    }, HttpMethod.Get, false)


@Builder
data class OrderHistoryParams(
    val category: Category,
    val symbol: String?,
    val limit: Int? = null
    )

@Serializable
data class OrderHistoryResultItem(
    val orderId: String,
    val orderLinkId: String
)


@Serializable
data class OrderHistoryListResult(
    override val category: Category,
    override val list: List<OrderHistoryResultItem>,
    override val nextPageCursor: String?
) : ListResult<OrderHistoryResultItem> {
}



@Serializable
data class OrderHistoryResponse(
    override val result: OrderHistoryListResult,
    override var nextUrl: String? = ""
) : APIResponseV5Paginatable<OrderHistoryResultItem>()


