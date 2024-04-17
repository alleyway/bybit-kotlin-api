package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder


suspend fun ByBitMarketClient.getKLine(
    params: KLineParams
): KLineResponse =
    byBitRestClient.call({
        path(
            "v5",
            "market",
            "kline",
        )
        params.category.let { parameters["category"] = it.toString() }
        parameters["symbol"] = params.symbol
        parameters["interval"] = params.interval
        params.start?.let { parameters["start"] = it.toString() }
        params.end?.let { parameters["end"] = it.toString() }
        params.limit?.let { parameters["limit"] = it.toString() }
    })

@Builder
data class KLineParams(
    val category: Category? = null,
    val symbol: String,
    val interval: String,
    val start: Long? = null,
    val end: Long? = null,
    val limit: Int? = null,
)

@Serializable
data class KlineListResult(
    val symbol: String,
    val category: Category,
    val list: List<List<String>>
)

@Serializable
data class KLineResponse(
    val result: KlineListResult,
) : APIResponseV5()
