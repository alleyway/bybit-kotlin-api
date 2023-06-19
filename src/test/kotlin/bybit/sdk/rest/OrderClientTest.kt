package bybit.sdk.rest


import bybit.sdk.rest.order.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class OrderClientTest {

	private var client: ByBitRestClient = ByBitRestClient(httpClientProvider = okHttpClientProvider)


	@Test
	fun orderHistoryTest() {

		val resp = client.orderClient.orderHistoryBlocking(
			OrderHistoryParams("spot", "BTCUSDT"))

		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
	}

	@Test
	fun orderHistoryPaginatedTest() {

		val resp = client.orderClient.orderHistoryPaginated(
			OrderHistoryParams("spot", "BTCUSDT", limit= 1)).asSequence().toList()

		resp.forEach {
			println(it.orderId)
		}

	}



	@Test
	fun placeOrderTest() {

		val resp = client.orderClient.placeOrderBlocking(
			PlaceOrderParams("spot",
				"BTCUSDT", Side.Buy, OrderType.Limit,
				"0.1",
				price = 24_000.toString()
				)
		)

		val orderId = resp.result.orderId

		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)

		Thread.sleep(5000)

		val cancelOrderResp = client.orderClient.cancelOrderBlocking(
			CancelOrderParams("spot",
				"BTCUSDT",
				orderId
			)
		)
		assertEquals(0, cancelOrderResp.retCode)
		assertEquals("OK", cancelOrderResp.retMsg)
	}


	@Test
	fun secondTest() {

		println("second test")

		assertTrue(5000 > 3000)

	}

}
