package bybit.sdk.rest.market

import bybit.sdk.rest.ListResult
import bybit.sdk.rest.Paginatable
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable


/** See [ByBitRestClient.getInstrumentsInfoBlocking] */
@SafeVarargs
suspend fun ByBitMarketClient.getInstrumentsInfo(
    params: GetInstrumentsInfoParamsV5
): TickersDTO =
    byBitRestClient.call({
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
    })

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
    override val nextPageCursor: String? = ""
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
enum class ContractType {
    InversePerpetual,
    LinearPerpetual,
    InverseFutures,
    LinearFutures
}

/*
{
        "symbol": "SUIHT",
        "baseCoin": "SUI",
        "quoteCoin": "HT",
        "innovation": "0",
        "status": "Trading",
        "marginTrading": "none",
        "lotSizeFilter": {
          "basePrecision": "0.01",
          "quotePrecision": "0.01",
          "minOrderQty": "0.01",
          "maxOrderQty": "5000",
          "minOrderAmt": "0.01",
          "maxOrderAmt": "5000"
        },
        "priceFilter": {
          "tickSize": "0.01"
        }
      },
 */


@Serializable
data class TickerDTO(
    val symbol: String,
//    val contractType: ContractType,
    //status: InstrumentStatusV5;
    val baseCoin: String?,
    val quoteCoin: String?,
//    val launchTime: String?,
//    val deliveryTime: String?,
//    val deliveryFeeRate: String?,
//    val priceScale: String?,
//    leverageFilter: {
//        minLeverage: string;
//        maxLeverage: string;
//        leverageStep: string;
//    };
//    priceFilter: {
//        minPrice: string;
//        maxPrice: string;
//        tickSize: string;
//    };
//    lotSizeFilter: {
//        maxOrderQty: string;
//        minOrderQty: string;
//        qtyStep: string;
//        postOnlyMaxOrderQty?: string;
//    }
//    val unifiedMarginTrade: Boolean?,
//    val fundingInterval: Int?,
//    val settleCoin: String?

)
