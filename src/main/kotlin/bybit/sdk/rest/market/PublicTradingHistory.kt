package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.rest.account.CoinResultItem
import bybit.sdk.shared.AccountType
import bybit.sdk.shared.Category
import com.thinkinglogic.builder.annotation.Builder
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
    val totalEquity: String,
    val accountIMRate: String,
    val totalMarginBalance: String,
    val totalInitialMargin: String,
    val accountType: AccountType,
    val totalAvailableBalance: String,
    val accountMMRate: String,
    val totalPerpUPL: String,
    val totalWalletBalance: String,
    val accountLTV: String,
    val coin: List<CoinResultItem>
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
