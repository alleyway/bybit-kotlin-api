package bybit.sdk.rest.position

import bybit.sdk.rest.APIResponseV5Paginatable
import bybit.sdk.rest.ListResult
import bybit.sdk.shared.Category
import bybit.sdk.shared.ExecType
import bybit.sdk.shared.OrderType
import bybit.sdk.shared.Side
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder


suspend fun ByBitPositionClient.closedPnLs(
    params: ClosedPnLParams
): ClosedPnLResponse =
    byBitRestClient.call({
        path(
            "v5",
            "position",
            "closed-pnl",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it }
        params.startTime?.let { parameters["startTime"] = it.toString() }
        params.endTime?.let { parameters["endTime"] = it.toString() }
        params.limit?.let { parameters["limit"] = it.toString() }
    }, HttpMethod.Get, false)


@Builder
data class ClosedPnLParams(
    val category: Category,
    val symbol: String? = "",
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = 50
    )

@Serializable
data class ClosedPnLResponseItem(
    val orderId: String,
    val symbol: String,
    val side: Side,
    val qty: String,
    val orderPrice: String,
    val orderType: OrderType,
    val execType: ExecType,
    val closedSize: String,
    val cumEntryValue: String,
    val avgEntryPrice: String,
    val cumExitValue: String,
    val avgExitPrice: String,
    val closedPnl: String,
    val fillCount: String,
    val leverage: String,
    val createdTime: String,
    val updatedTime: String
)

@Serializable
data class ClosedPnLResponseListResult(
    override val nextPageCursor: String? = "",
    override val category: Category,
    override val list: List<ClosedPnLResponseItem>
) : ListResult<ClosedPnLResponseItem> {
}

@Serializable
data class ClosedPnLResponse(
    override val result: ClosedPnLResponseListResult,
    override var nextUrl: String? = ""
) : APIResponseV5Paginatable<ClosedPnLResponseItem>()


