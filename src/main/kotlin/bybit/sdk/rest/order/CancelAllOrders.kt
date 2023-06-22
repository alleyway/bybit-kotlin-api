package bybit.sdk.rest.order

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject


suspend fun ByBitOrderClient.cancelAllOrders(
    params: CancelAllOrdersParams
): CancelAllOrdersResponse =
    byBitRestClient.call({
        path(
            "v5",
            "order",
            "cancel-all",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it }
        params.baseCoin?.let { parameters["baseCoin"] = it }
        params.settleCoin?.let { parameters["settleCoin"] = it }
    }, HttpMethod.Post, false)


@Builder
data class CancelAllOrdersParams(
    val category: Category,
    val symbol: String? = null,
    val baseCoin: String? = null,
    val settleCoin: String? = null
)

@Serializable
data class CancelAllOrdersResultSpot(
    val success: String
)

@Serializable
data class CancelAllOrdersResultItemOther(
    val orderId: String,
    val orderLinkId: String
)

@Serializable
data class CancelAllOrdersResultOther(
    val list: List<CancelAllOrdersResultItemOther>
)


object CancelAllOrdersResponseSerializer :
    JsonContentPolymorphicSerializer<CancelAllOrdersResponse>(CancelAllOrdersResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CancelAllOrdersResponse> {

        val resultListElement = element.jsonObject["result"]?.jsonObject?.get("list")

        return when {
            resultListElement is JsonArray -> CancelAllOrdersResponse.CancelAllOrdersResponseOther.serializer()
            else -> CancelAllOrdersResponse.CancelAllOrdersResponseSpot.serializer()
        }
    }
}

@Serializable(with = CancelAllOrdersResponseSerializer::class)
sealed class CancelAllOrdersResponse: APIResponseV5() {

    @Serializable
    data class CancelAllOrdersResponseSpot(
        val result: CancelAllOrdersResultSpot
    ) : CancelAllOrdersResponse()

    @Serializable
    data class CancelAllOrdersResponseOther(
        val result: CancelAllOrdersResultOther
    ) : CancelAllOrdersResponse()

}








