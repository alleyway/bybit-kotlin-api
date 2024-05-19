package bybit.sdk.rest.position

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import bybit.sdk.shared.PositionStatus
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
    val positionIdx: Int,
    val riskId: Int,
    val riskLimitValue: String,
    val symbol: String,
    val side: Side,
    val size: String,
    val avgPrice: String,
    val positionValue: String,
    val tradeMode: Int,
    val positionStatus: PositionStatus,
    val autoAddMargin: Int,
    val adlRankIndicator: Int,
    val leverage: String,
    val positionBalance: String,
    val markPrice: String,
    val liqPrice: String,
    val bustPrice: String,
    val positionMM: String,
    val positionIM: String,
    val takeProfit: String,
    val stopLoss: String,
    val trailingStop: String,
    val unrealisedPnl: String,
    val curRealisedPnl: String,
    val seq: Long,
    val isReduceOnly: Boolean,
    val mmrSysUpdatedTime: String,
    val leverageSysUpdatedTime: String,
    val sessionAvgPrice: String,
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
