package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder


suspend fun ByBitMarketClient.getTickers(
    params: TickersParams
): TickersResponse =
    byBitRestClient.call({
        path(
            "v5",
            "market",
            "tickers",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.baseCoin?.let { parameters["baseCoin"] = it.toString() }
        params.expDate?.let { parameters["expDate"] = it.toString() }
    })

@Builder
data class TickersParams(
    val category: Category,
    val symbol: String,
    val baseCoin: String? = null,
    val expDate: String? = null,
)

@Serializable
data class TickersResultItem(
    val symbol: String,
    val lastPrice:Double,
    val indexPrice: Double,
    val markPrice: Double,
    val prevPrice24h: Double,
    val price24hPcnt: Double,
    val highPrice24h: Double,
    val lowPrice24h: Double,
    val prevPrice1h: Double,
    val openInterest: Double,
    val openInterestValue: Double,
    val turnover24h: Double,
    val volume24h: Double,
    val fundingRate: Double,
    val nextFundingTime: Long,
//    val predictedDeliveryPrice: ,
//    val basisRate: ,
//    val deliveryFeeRate: ,
//    val deliveryTime: 0,
    val bid1Price: Double,
    val bid1Size: Double,
    val ask1Price: Double,
    val ask1Size: Double,
)

@Serializable
data class TickersResponseResult(
    val category: Category,
    val list: List<TickersResultItem>,
)


@Serializable
data class TickersResponse(
    val result: TickersResponseResult,
) : APIResponseV5()
