package bybit.sdk.rest.position

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import bybit.sdk.shared.TradeMode
import lombok.Builder
import io.ktor.http.*


suspend fun ByBitPositionClient.switchIsolated(
    params: SwitchIsolatedParams
): APIResponseV5 =
    byBitRestClient.call({
        path(
            "v5",
            "position",
            "switch-isolated",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.tradeMode.let { parameters["tradeMode"] = it.ordinal.toString() }
        params.buyLeverage.let { parameters["buyLeverage"] = it }
        params.sellLeverage.let { parameters["sellLeverage"] = it }
    }, HttpMethod.Post, false)

@Builder
data class SwitchIsolatedParams(
    val category: Category,
    val symbol: String,
    val tradeMode: TradeMode,
    val sellLeverage: String,
    val buyLeverage: String,
)
