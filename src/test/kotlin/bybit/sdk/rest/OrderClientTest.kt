package bybit.sdk.rest


import bybit.sdk.RateLimitReachedException
import bybit.sdk.properties.ByBitProperties
import bybit.sdk.rest.order.*
import bybit.sdk.shared.Category
import bybit.sdk.shared.OrderType
import bybit.sdk.shared.Side
import kotlinx.coroutines.*
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class OrderClientTest {

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private val jobs = mutableListOf<Job>()

    private var restClient: ByBitRestClient =
        ByBitRestClient(
            apiKey = System.getenv("BYBIT_API_KEY") ?: ByBitProperties.APIKEY,
            secret = System.getenv("BYBIT_SECRET") ?: ByBitProperties.SECRET,
            testnet = true,
//            httpClientProvider = okHttpClientProvider
        )

    fun add(block: suspend CoroutineScope.() -> Unit): Job {
        val job = scope.launch(block = block)
        jobs.add(job)
        return job
    }

    @Test
    fun orderHistoryTest() {
        val resp = restClient.orderClient.orderHistoryBlocking(
            OrderHistoryParams(Category.spot, "BTCUSDT")
        )

        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)
    }

    @Test
    fun orderHistoryPaginatedTest() {
        val resp = restClient.orderClient.orderHistoryPaginated(
            OrderHistoryParams(Category.spot, "BTCUSD", limit = 10)
        ).asStream().toList()

        resp.forEach {
            println(it.orderId)
        }
    }


    @Test
    fun placeOrderTest() {
        val resp = restClient.orderClient.placeOrderBlocking(
            PlaceOrderParams(
                Category.inverse,
                "BTCUSD", Side.Buy, OrderType.Limit,
                "1.0",
                price = 50_000.toString()
            )
        )

        val orderId = resp.result.orderId

        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)

        Thread.sleep(50_000)

        val cancelOrderResp = restClient.orderClient.cancelOrderBlocking(
            CancelOrderParams(
                Category.inverse,
                "BTCUSD",
                orderId
            )
        )
        assertEquals(0, cancelOrderResp.retCode)
        assertEquals("OK", cancelOrderResp.retMsg)
    }

    @Test
    fun ordersOpenTest() {

        restClient.orderClient.placeOrderBlocking(
            PlaceOrderParams(
                Category.inverse,
                "BTCUSD", Side.Buy, OrderType.Limit,
                "1.0",
                price = 50_000.toString()
            )
        )
        val resp = restClient.orderClient.ordersOpenPaginated(
            OrdersOpenParams(Category.inverse)
        )
        val items = resp.asStream().toList()
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


    @Test
    @Ignore
    fun orderRateLimitTest() {

        val nThreads = 10
        val loop = 100


        Thread.sleep(4_000)
        val random = java.util.Random()

        restClient.orderClient.cancelAllOrdersBlocking(
            CancelAllOrdersParams(Category.inverse, "BTCUSD")
        )

        val resp = restClient.orderClient.placeOrderBlocking(
            PlaceOrderParams(
                Category.inverse,
                "BTCUSD", Side.Buy, OrderType.Limit,
                "1.0",
                price = 50_000.toString()
            )
        )

        val orderId = resp.result.orderId

        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)

        Thread.sleep(5000)

        repeat(nThreads) {
            jobs.add(
                scope.launch {
                    assertDoesNotThrow {
                        repeat(loop) {
                            //
                            try {
                                val r = restClient.orderClient.amendOrderBlocking(
                                    AmendOrderParams(
                                        Category.inverse,
                                        "BTCUSD",
                                        orderId = orderId,
                                        qty = random.nextInt(100, 200).toString(),
                                        price = 20_100.toString()
                                    )
                                )
                                println(r.result.orderId)
                            } catch (e: RateLimitReachedException) {
                                println("Rate Limit Exception: ${e.message}")
                            }
                            catch (e: Exception) {
                                e.printStackTrace()
                            }


                            Thread.sleep(1)
                        }

                        Thread.sleep(1)

                        repeat(10) {
                            //

                            restClient.orderClient.amendOrderBlocking(
                                AmendOrderParams(
                                    Category.inverse,
                                    "BTCUSD",
                                    orderId = orderId,
                                    qty = random.nextInt(200, 400).toString(),
                                    price = 20_100.toString()
                                )
                            )

                        }
                    }
                }
            )
        }

        assertEquals(nThreads, jobs.size)
        runBlocking {
            jobs.joinAll()
        }
        println("ALL JOINED!! Remaining sleep for 3 seconds")
        Thread.sleep(3_000)

    }

}
