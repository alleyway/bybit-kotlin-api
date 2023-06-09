package bybit.sdk.rest.market

import bybit.sdk.rest.ByBitRestOption
import bybit.sdk.rest.ListResult
import bybit.sdk.rest.Paginatable
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable


/** See [ByBitRestClient.getInstrumentsInfoBlocking] */
@SafeVarargs
suspend fun ByBitMarketClient.getInstrumentsInfo(
    params: GetInstrumentsInfoParamsV5,
    vararg opts: ByBitRestOption
): TickersDTO =
    byBitRestClient.fetchResult({
        path(
            "v5",
            "market",
            "instruments-info",
        )
        params.category.let { parameters["category"] = it }
        params.symbol?.let { parameters["symbol"] = it }
        params.baseCoin?.let { parameters["baseCoin"] = it }
        params.limit?.let { parameters["limit"] = it.toString() }
        parameters["status"] = "Trading"
    }, *opts)

@Builder
data class GetInstrumentsInfoParamsV5(
    val category: String, // 'spot' | 'linear' | 'inverse' | 'option'
    // status?
    val symbol: String? = null,
    val baseCoin: String? = null,
    val limit: Int? = null,
    val cursor: String? = null
)

@Serializable
data class TickersListResult(
    override val category: String,
    override val list: List<TickerDTO>,
    override val nextPageCursor: String?
) : ListResult<TickerDTO> {
}

@Serializable
data class TickersDTO(
    val retCode: Int = 0,
    val retMsg: String = "OK",
    val time: Long = 0,
    override val result: TickersListResult? = null,
    override var nextUrl: String? = "",
) : Paginatable<TickerDTO>

@Serializable
data class TickerDTO(
    val symbol: String
)
