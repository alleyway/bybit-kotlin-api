package bybit.sdk.rest


import bybit.sdk.properties.ByBitProperties
import bybit.sdk.rest.market.InstrumentsInfoParams
import bybit.sdk.rest.market.PublicTradingHistoryParams
import bybit.sdk.shared.Category
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MarketClientTest {

    private var restClient: ByBitRestClient =
        ByBitRestClient(
            apiKey = System.getenv("BYBIT_API_KEY") ?: ByBitProperties.APIKEY,
            secret = System.getenv("BYBIT_SECRET") ?: ByBitProperties.SECRET,
            testnet = true,
//            httpClientProvider = okHttpClientProvider
        )

    @Test
    fun getInstrumentsInfo() {

        val resp = restClient.marketClient.getInstrumentsInfoBlocking(
            InstrumentsInfoParams(
                category = Category.inverse
            )
        )
		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
    }

    @Test
    fun getPublicTradingHistory() {

        val resp = restClient.marketClient.getPublicTradingHistoryBlocking(
            PublicTradingHistoryParams(
                category = Category.inverse,
                symbol = "BTCUSD"
            )
        )
        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)
    }

    @Test
    fun listSupportedInstruments() {

        val params = InstrumentsInfoParams(
            limit = 4, // apparently not allowed to pass a limit for spot
            category = Category.inverse
//			category = "linear"
        )

        val resp = restClient.marketClient.listSupportedInstruments(params).asSequence().toList()

        resp.forEach {
            println(it.symbol)
        }

        println("finished fetching inverse instruments, found ${resp.size}")

        assertTrue(resp.size > 8)
    }


}
