package bybit.sdk.rest


import bybit.sdk.rest.order.*
import bybit.sdk.shared.Category
import bybit.sdk.shared.OrderType
import bybit.sdk.shared.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class OrderClientTest {

	private var client: ByBitRestClient = ByBitRestClient(httpClientProvider = okHttpClientProvider)


	@Test
	fun orderHistoryTest() {

		val resp = client.orderClient.orderHistoryBlocking(
			OrderHistoryParams(Category.spot, "BTCUSDT"))

		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
	}

	@Test
	fun orderHistoryPaginatedTest() {

		val resp = client.orderClient.orderHistoryPaginated(
			OrderHistoryParams(Category.spot, "BTCUSDT", limit= 1)).asSequence().toList()

		resp.forEach {
			println(it.orderId)
		}

	}



	@Test
	fun placeOrderTest() {

		val resp = client.orderClient.placeOrderBlocking(
			PlaceOrderParams(Category.spot,
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
			CancelOrderParams(Category.spot,
				"BTCUSDT",
				orderId
			)
		)
		assertEquals(0, cancelOrderResp.retCode)
		assertEquals("OK", cancelOrderResp.retMsg)
	}

	@Test
	fun ordersOpenTest() {
		val resp = client.orderClient.ordersOpenPaginated(
			OrdersOpenParams(Category.spot)
		)
		val items = resp.asSequence().toList()
		assertTrue(items.size > 0)
	}


	@Test
	fun cancelAllOrdersTest() {
		val spotResponse = client.orderClient.cancelAllOrdersBlocking(
			CancelAllOrdersParams(Category.spot)
		)
		assertTrue(spotResponse.retCode == 0)
		assertTrue(spotResponse is CancelAllOrdersResponse.CancelAllOrdersResponseSpot)

		val linearResponse = client.orderClient.cancelAllOrdersBlocking(
			CancelAllOrdersParams(Category.linear, settleCoin = "USDT")
		)
		assertTrue(linearResponse.retCode == 0)
		assertTrue(linearResponse is CancelAllOrdersResponse.CancelAllOrdersResponseOther)
	}


}
