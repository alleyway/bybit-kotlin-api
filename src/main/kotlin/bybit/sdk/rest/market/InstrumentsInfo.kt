package bybit.sdk.rest.market

import bybit.sdk.rest.APIResponseV5Paginatable
import bybit.sdk.rest.ListResult
import bybit.sdk.shared.Category
import bybit.sdk.shared.ContractType
import bybit.sdk.shared.OptionsType
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import lombok.Builder
import java.util.*


/** See [ByBitRestClient.getInstrumentsInfoBlocking] */

suspend fun ByBitMarketClient.getInstrumentsInfo(
    params: InstrumentsInfoParams
): InstrumentsInfoResponse<InstrumentsInfoResultItem> =
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
data class LotSizeFilterSpot(
    val basePrecision: String,
    val quotePrecision: String,
    val minOrderQty: String,
    val maxOrderQty: String,
    val minOrderAmt: String,
    val maxOrderAmt: String
)

@Serializable
data class LotSizeFilterShared(
    val maxOrderQty: String,
    val minOrderQty: String,
    val qtyStep: String
)


@Serializable(with = InstrumentsInfoResultItemSerializer::class)
sealed class InstrumentsInfoResultItem {
    open val symbol: String = ""
    open val baseCoin: String = ""
    open val quoteCoin: String = ""
    open val status: String = ""



    @Serializable
    data class InstrumentsInfoResultItemLinearInverse(
        override val symbol: String = "",
        override val baseCoin: String = "",
        override val quoteCoin: String = "",
        override val status: String = "",
        val lotSizeFilter: LotSizeFilterShared,
        val contractType: ContractType,
        val fundingInterval: Int,
    ) : InstrumentsInfoResultItem()


    @Serializable
    data class InstrumentsInfoResultItemOption(
        override val symbol: String = "",
        override val baseCoin: String = "",
        override val quoteCoin: String = "",
        override val status: String = "",
        val lotSizeFilter: LotSizeFilterShared,
        val optionsType: OptionsType
    ) : InstrumentsInfoResultItem()


    @Serializable
    data class InstrumentsInfoResultItemSpot(
        override val symbol: String = "",
        override val baseCoin: String = "",
        override val quoteCoin: String = "",
        override val status: String = "",
        val lotSizeFilter: LotSizeFilterSpot,

        val marginTrading: String,
    ) : InstrumentsInfoResultItem()


}


@Serializable
data class InstrumentsInfoListResult<T : InstrumentsInfoResultItem>(
    override val category: Category,
    override val list: List<T>,
    override val nextPageCursor: String? = ""
) : ListResult<InstrumentsInfoResultItem>


object InstrumentsInfoResultItemSerializer :
    JsonContentPolymorphicSerializer<InstrumentsInfoResultItem>(InstrumentsInfoResultItem::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<InstrumentsInfoResultItem> = when {
        "contractType" in element.jsonObject -> InstrumentsInfoResultItem.InstrumentsInfoResultItemLinearInverse.serializer()
        "optionsType" in element.jsonObject -> InstrumentsInfoResultItem.InstrumentsInfoResultItemOption.serializer()
        else -> InstrumentsInfoResultItem.InstrumentsInfoResultItemSpot.serializer()

    }
}

@Serializable
data class InstrumentsInfoResponse<T : InstrumentsInfoResultItem>(
    override val result: InstrumentsInfoListResult<T>? = null,
    override var nextUrl: String? = "",
) : APIResponseV5Paginatable<InstrumentsInfoResultItem>()
