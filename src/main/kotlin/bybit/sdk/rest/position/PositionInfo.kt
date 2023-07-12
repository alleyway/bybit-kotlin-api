package bybit.sdk.rest.position

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import bybit.sdk.shared.Side
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*
import kotlinx.serialization.Serializable

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
    val positionValue: String,
    val tradeMode: Int,

    val markPrice: String,

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
