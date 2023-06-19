package bybit.sdk.rest


import bybit.sdk.rest.order.OrderHistoryParams
import bybit.sdk.rest.order.OrderType
import bybit.sdk.rest.order.PlaceOrderParams
import bybit.sdk.rest.order.Side
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
				"BTCUSDT", Side.Buy, OrderType.Market,
				"10")
		)

		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
	}


	@Test
	fun secondTest() {

		println("second test")

		assertTrue(5000 > 3000)

	}

}
