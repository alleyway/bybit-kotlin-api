package bybit.sdk.rest.account

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder

suspend fun ByBitAccountClient.getFeeRate(
    params: FeeRateParams
): FeeRateResponse =
    byBitRestClient.call({
        path(
            "v5",
            "account",
            "fee-rate",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it }
        params.baseCoin?.let { parameters["baseCoin"] = it }
    }, HttpMethod.Get, false)

@Builder
data class FeeRateParams(
    val category: Category,
    val symbol: String? = null,
    val baseCoin: String? = null,
)

@Serializable
data class FeeRateResultItem(
    val symbol: String,
    val baseCoin: String? = null,
    val takerFeeRate: String,
    val makerFeeRate: String,
)

@Serializable
data class FeeRateListResult(
    val category: Category? = null,
    val list: List<FeeRateResultItem>
)

@Serializable
data class FeeRateResponse(
    val result: FeeRateListResult,
) : APIResponseV5()
