package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import bybit.sdk.shared.Side
import lombok.Builder
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import java.util.*


/** See [ByBitRestClient.getInstrumentsInfoBlocking] */

suspend fun ByBitMarketClient.getPublicTradingHistory(
    params: PublicTradingHistoryParams
): PublicTradingHistoryResponse =
    byBitRestClient.call({
        path(
            "v5",
            "market",
            "recent-trade",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it }
        params.baseCoin?.let { parameters["baseCoin"] = it }
        params.limit?.let { parameters["limit"] = it.toString() }
    })

@Builder
data class PublicTradingHistoryParams(
    val category: Category,
    val symbol: String? = null,
    val baseCoin: String? = null,
    val limit: Int? = 1000
)

@Serializable
data class PublicTradingHistoryResultItem(
    val execId: String,
    val symbol: String,
    val price: String,
    val size: String,
    val side: Side,
    val time: String,
    val isBlockTrade: Boolean,
)

@Serializable
data class PublicTradingHistoryListResult(
    val category: Category,
    val list: List<PublicTradingHistoryResultItem>
)

@Serializable
data class PublicTradingHistoryResponse(
    val result: PublicTradingHistoryListResult,
) : APIResponseV5()
