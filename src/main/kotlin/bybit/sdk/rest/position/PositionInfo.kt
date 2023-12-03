package bybit.sdk.rest.position

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import bybit.sdk.shared.Side
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder

suspend fun ByBitPositionClient.getPositionInfo(
    params: PositionInfoParams
): PositionInfoResponse =
    byBitRestClient.call({
        path(
            "v5",
            "position",
            "list",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol?.let { parameters["symbol"] = it }
        params.settleCoin?.let { parameters["settleCoin"] = it }
    }, HttpMethod.Get, false)

@Builder
data class PositionInfoParams(
    val category: Category,
    val symbol: String? = null,
    val settleCoin: String? = null,
)

@Serializable
data class PositionInfoListResultItem(
    val symbol: String,
    val side: Side,
    val size: String,
    val avgPrice: String,
    val leverage: String,
    val positionValue: String,
    val positionBalance: String,
    val tradeMode: Int,
    val liqPrice: String,
    val markPrice: String,
    val positionIM: String,
    val positionMM: String,
    val createdTime: String,
    val updatedTime: String,
)

@Serializable
data class PositionInfoListResult(
     val list: List<PositionInfoListResultItem>
)

@Serializable
data class PositionInfoResponse(
    val result: PositionInfoListResult,
) : APIResponseV5()
