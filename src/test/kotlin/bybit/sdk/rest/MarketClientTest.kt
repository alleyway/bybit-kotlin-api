package bybit.sdk.rest


import bybit.sdk.rest.market.GetInstrumentsInfoParamsV5
import kotlin.test.Test
import kotlin.test.assertTrue

internal class MarketClientTest {


	@Test
	fun getInstrumentsInfo() {

		val bybitKey = "blah"
		val bybitSecret = "blah"

		val bybitClient = ByBitRestClient(
			bybitKey, bybitSecret,
			httpClientProvider = okHttpClientProvider
		)
		val resp = bybitClient.marketClient.getInstrumentsInfoBlocking(GetInstrumentsInfoParamsV5(
			category = "inverse"
		))
//		assertEquals(0, resp.retCode)
//		assertEquals("OK", resp.retMsg)
	}

	@Test
	fun listSupportedInstruments() {

		val bybitKey = "blah"
		val bybitSecret = "blah"

		val bybitClient = ByBitRestClient(
			bybitKey, bybitSecret,
			httpClientProvider = okHttpClientProvider
		)
		val params = GetInstrumentsInfoParamsV5(
			limit = 10, // apparently not allowed to pass a limit for spot
			category = "inverse"
//			category = "linear"
		)

		val resp = bybitClient.marketClient.listSupportedInstruments(params).asSequence().toList()

		resp.forEach {
			println(it.symbol)
		}

		println("finished fetching inverse instruments, found ${resp.size}")

		assertTrue(resp.size > 10)
	}


}
