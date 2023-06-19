package bybit.sdk.rest


import bybit.sdk.rest.market.GetInstrumentsInfoParamsV5
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MarketClientTest {
    private var client: ByBitRestClient = ByBitRestClient(httpClientProvider = okHttpClientProvider)


    @Test
    fun getInstrumentsInfo() {

        val resp = client.marketClient.getInstrumentsInfoBlocking(
            GetInstrumentsInfoParamsV5(
                category = "inverse"
            )
        )
		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
    }

    @Test
    fun listSupportedInstruments() {

        val params = GetInstrumentsInfoParamsV5(
            limit = 4, // apparently not allowed to pass a limit for spot
            category = "inverse"
//			category = "linear"
        )

        val resp = client.marketClient.listSupportedInstruments(params).asSequence().toList()

        resp.forEach {
            println(it.symbol)
        }

        println("finished fetching inverse instruments, found ${resp.size}")

        assertTrue(resp.size > 8)
    }


}
