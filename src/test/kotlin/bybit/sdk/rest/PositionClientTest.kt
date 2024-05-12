package bybit.sdk.rest


import bybit.sdk.properties.ByBitProperties
import bybit.sdk.rest.position.PositionInfoParams
import bybit.sdk.shared.Category
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PositionClientTest {

    private var restClient: ByBitRestClient =
        ByBitRestClient(
            apiKey = System.getenv("BYBIT_API_KEY") ?: ByBitProperties.APIKEY,
            secret = System.getenv("BYBIT_SECRET") ?: ByBitProperties.SECRET,
            testnet = true,
//            httpClientProvider = okHttpClientProvider
        )

    @Test
    fun getPositionInfo() {
        val resp = restClient.positionClient.getPositionInfoBlocking(
            PositionInfoParams(Category.inverse, settleCoin = "BTC")
        )
        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)
    }
}
