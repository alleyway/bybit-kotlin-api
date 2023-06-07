package bybit.sdk.rest


import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ContractClientTest {


	@Test
	fun getServerTime() {
		println("getServerTime()")

		val bybitKey = "blah"
		val bybitSecret = "blah"

		val bybitClient = ByBitRestClient(
			bybitKey, bybitSecret,
			httpClientProvider = okHttpClientProvider
		)
		val resp = bybitClient.contractClient.getServerTimeBlocking()
		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
	}


	@Test
	fun secondTest() {

		println("second test")

		assertTrue(5000 > 3000)

	}

}
