package bybit.sdk.rest


import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ContractClientTest {

	private var client: ByBitRestClient = ByBitRestClient(httpClientProvider = okHttpClientProvider)

	@Test
	fun getServerTime() {
		println("getServerTime()")

		val resp = client.contractClient.getServerTimeBlocking()
		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
	}


	@Test
	fun secondTest() {

		println("second test")

		assertTrue(5000 > 3000)

	}

}
