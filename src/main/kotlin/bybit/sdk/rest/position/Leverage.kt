package bybit.sdk.rest.position

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.Category
import com.thinkinglogic.builder.annotation.Builder
import io.ktor.http.*


suspend fun ByBitPositionClient.setLeverage(
    params: LeverageParams
): APIResponseV5 =
    byBitRestClient.call({
        path(
            "v5",
            "position",
            "set-leverage",
        )
        params.category.let { parameters["category"] = it.toString() }
        params.symbol.let { parameters["symbol"] = it }
        params.buyLeverage.let { parameters["buyLeverage"] = it }
        params.sellLeverage.let { parameters["sellLeverage"] = it }
    }, HttpMethod.Post, false)

@Builder
data class LeverageParams(
    val category: Category,
    val symbol: String,
    val buyLeverage: String,
    val sellLeverage: String,
)
