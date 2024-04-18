package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import lombok.Builder
import java.util.*


/** See [ByBitRestClient.getKlineBlocking] */

suspend fun ByBitMarketClient.getKline(
    params: KlineParams
): KlineResponse =
    byBitRestClient.call({
        path(
            "v5",
            "market",
            "kline",
        )
        params.category?.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.limit?.let { parameters["limit"] = it.toString() }
        params.interval.let { parameters["interval"] = it.toString() }
        params.start?.let { parameters["start"] = it.toString() }
        params.end?.let { parameters["end"] = it.toString() }
    })

@Builder
data class KlineParams(
    val category: Category? = Category.linear,
    val symbol: String,
    val interval: String,
    val limit: Int? = null,
    val start: Long? = null,
    val end: Long? = null,
)


@Serializable
data class KlineResponse(
    val result: KlineListResult,
) : APIResponseV5()

@Serializable
data class KlineListResult(
    val symbol: String,
    val category: Category,
    val list: List<List<Double>>
)
