package bybit.sdk.rest


import bybit.sdk.rest.order.*
import bybit.sdk.shared.Category
import bybit.sdk.shared.OrderType
import bybit.sdk.shared.Side
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class OrderClientTest {

	private var restClient: ByBitRestClient =
		ByBitRestClient(
			apiKey = System.getenv("BYBIT_API_KEY"),
			secret = System.getenv("BYBIT_SECRET"),
			testnet = true,
			httpClientProvider = okHttpClientProvider)

	@Test
	fun orderHistoryTest() {
		val resp = restClient.orderClient.orderHistoryBlocking(
			OrderHistoryParams(Category.spot, "BTCUSDT"))

		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
	}

	@Test
	fun orderHistoryPaginatedTest() {
		val resp = restClient.orderClient.orderHistoryPaginated(
			OrderHistoryParams(Category.spot, "BTCUSD", limit= 10)).asSequence().toList()

		resp.forEach {
			println(it.orderId)
		}
	}



	@Test
	fun placeOrderTest() {
		val resp = restClient.orderClient.placeOrderBlocking(
			PlaceOrderParams(Category.inverse,
				"BTCUSD", Side.Buy, OrderType.Limit,
				"1.0",
				price = 5_000.toString()
				)
		)

		val orderId = resp.result.orderId

		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)

		Thread.sleep(5000)

		val cancelOrderResp = restClient.orderClient.cancelOrderBlocking(
			CancelOrderParams(Category.inverse,
				"BTCUSD",
				orderId
			)
		)
		assertEquals(0, cancelOrderResp.retCode)
		assertEquals("OK", cancelOrderResp.retMsg)
	}

	@Test
	fun ordersOpenTest() {

		val newOrderResp = restClient.orderClient.placeOrderBlocking(
			PlaceOrderParams(Category.inverse,
				"BTCUSD", Side.Buy, OrderType.Limit,
				"1.0",
				price = 5_000.toString()
			)
		)
		val resp = restClient.orderClient.ordersOpenPaginated(
			OrdersOpenParams(Category.inverse)
		)
		val items = resp.asSequence().toList()
		assertTrue(items.size > 0)
	}


	@Test
	fun cancelAllOrdersTest() {
		val spotResponse = restClient.orderClient.cancelAllOrdersBlocking(
			CancelAllOrdersParams(Category.spot)
		)
		assertTrue(spotResponse.retCode == 0)
		assertTrue(spotResponse is CancelAllOrdersResponse.CancelAllOrdersResponseSpot)

		val linearResponse = restClient.orderClient.cancelAllOrdersBlocking(
			CancelAllOrdersParams(Category.linear, settleCoin = "USDT")
		)
		assertTrue(linearResponse.retCode == 0)
		assertTrue(linearResponse is CancelAllOrdersResponse.CancelAllOrdersResponseOther)
	}


}
