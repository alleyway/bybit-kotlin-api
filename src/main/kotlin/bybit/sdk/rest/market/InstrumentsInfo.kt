package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5Paginatable
import bybit.sdk.rest.ListResult
import bybit.sdk.shared.Category
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable


/** See [ByBitRestClient.getInstrumentsInfoBlocking] */

suspend fun ByBitMarketClient.getInstrumentsInfo(
    params: InstrumentsInfoParams
): InstrumentsInfoResponse =
    byBitRestClient.call({
        path(
            "v5",
            "market",
            "instruments-info",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it }
        params.baseCoin?.let { parameters["baseCoin"] = it }
        params.limit?.let { parameters["limit"] = it.toString() }
        parameters["status"] = "Trading"
    })

@Builder
data class InstrumentsInfoParams(
    val category: Category,
    // status?
    val symbol: String? = null,
    val baseCoin: String? = null,
    val limit: Int? = null,
    val cursor: String? = null
)

@Serializable
data class InstrumentsInfoResultItem(
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


@Serializable
data class InstrumentsInfoListResult(
    override val category: Category,
    override val list: List<InstrumentsInfoResultItem>,
    override val nextPageCursor: String? = ""
) : ListResult<InstrumentsInfoResultItem> {
}

@Serializable
data class InstrumentsInfoResponse(
    override val result: InstrumentsInfoListResult? = null,
    override var nextUrl: String? = "",
) : APIResponseV5Paginatable<InstrumentsInfoResultItem>()
