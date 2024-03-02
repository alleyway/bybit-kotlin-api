package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5Paginatable
import bybit.sdk.rest.ListResult
import bybit.sdk.shared.Category
import bybit.sdk.shared.IntervalTime
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder


suspend fun ByBitMarketClient.getOpenInterest(
    params: OpenInterestParams
): OpenInterestResponse =
    byBitRestClient.call({
        path(
            "v5",
            "market",
            "open-interest",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.intervalTime.let { parameters["intervalTime"] = it.stringValue }
        params.startTime?.let { parameters["startTime"] = it.toString() }
        params.endTime?.let { parameters["endTime"] = it.toString() }
        params.limit?.let { parameters["limit"] = it.toString() }
    })

@Builder
data class OpenInterestParams(
    val category: Category,
    val symbol: String,
    val intervalTime: IntervalTime,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int? = null,
    val cursor: String? = null
)

@Serializable
data class OpenInterestResultItem(
    val openInterest: String,
    val timestamp: String
)

@Serializable
data class OpenInterestListResult(
    override val category: Category,
    override val list: List<OpenInterestResultItem>,
    override val nextPageCursor: String?
) : ListResult<OpenInterestResultItem>

@Serializable
data class OpenInterestResponse(
    override val result: OpenInterestListResult,
    override var nextUrl: String? = ""
) : APIResponseV5Paginatable<OpenInterestResultItem>()
