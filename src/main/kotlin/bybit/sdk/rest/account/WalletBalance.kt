package bybit.sdk.rest.account

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.AccountType
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable

suspend fun ByBitAccountClient.getWalletBalance(
    params: WalletBalanceParams
): WalletBalanceResponse =
    byBitRestClient.call({
        path(
            "v5",
            "account",
            "wallet-balance",
        )
        params.accountType.let { parameters["accountType"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it.joinToString(",") }
    }, HttpMethod.Get, false)

@Builder
data class WalletBalanceParams(
    val accountType: AccountType,
    val symbol: List<String>? = null,
)

@Serializable
data class CoinResultItem(
    val coin: String,
    val equity: String,
    val usdValue: String,
    val walletBalance: String,
    val borrowAmount: String,
    val availableToBorrow: String,
    val availableToWithdraw: String,
    val accruedInterest: String,
    val totalOrderIM: String,
    val totalPositionIM: String,
    val totalPositionMM: String,
    val unrealisedPnl: String,
    val cumRealisedPnl: String
)

@Serializable
data class WalletBalanceResultItem(
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
data class WalletBalanceListResult(
     val list: List<WalletBalanceResultItem>
)

@Serializable
data class WalletBalanceResponse(
    val result: WalletBalanceListResult,
) : APIResponseV5()
