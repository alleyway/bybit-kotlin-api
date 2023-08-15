package bybit.sdk.rest.order

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.RequestIterator
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class ByBitOrderClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    fun placeOrderBlocking(params: PlaceOrderParams):
            PlaceOrderResponse = runBlocking { placeOrder(params) }

    /** See [placeOrderBlocking] */
    fun placeOrder(
        params: PlaceOrderParams,
        callback: ByBitRestApiCallback<PlaceOrderResponse>
    ) = coroutineToRestCallback(callback, { placeOrder(params) })

    fun amendOrderBlocking(params: AmendOrderParams):
            AmendOrderResponse = runBlocking { amendOrder(params) }

    /** See [amendOrderBlocking] */
    fun amendOrder(
        params: AmendOrderParams,
        callback: ByBitRestApiCallback<AmendOrderResponse>
    ) = coroutineToRestCallback(callback, { amendOrder(params) })


    fun cancelOrderBlocking(params: CancelOrderParams):
            CancelOrderResponse = runBlocking { cancelOrder(params) }

    /** See [placeOrderBlocking] */
    fun cancelAllOrders(
        params: CancelAllOrdersParams,
        callback: ByBitRestApiCallback<CancelAllOrdersResponse>
    ) = coroutineToRestCallback(callback, { cancelAllOrders(params) })

    fun cancelAllOrdersBlocking(params: CancelAllOrdersParams):
            CancelAllOrdersResponse = runBlocking { cancelAllOrders(params) }

    /** See [placeOrderBlocking] */
    fun cancelOrder(
        params: CancelOrderParams,
        callback: ByBitRestApiCallback<CancelOrderResponse>
    ) = coroutineToRestCallback(callback, { cancelOrder(params) })



    fun orderHistoryBlocking(params: OrderHistoryParams):
            OrderHistoryResponse = runBlocking { orderHistory(params) }

    /** See [orderHistoryBlocking] */
    fun orderHistory(
        params: OrderHistoryParams,
        callback: ByBitRestApiCallback<OrderHistoryResponse>
    ) = coroutineToRestCallback(callback, { orderHistory(params) })

    fun orderHistoryPaginated(
        params: OrderHistoryParams,
    ): RequestIterator<OrderHistoryResultItem> =
        RequestIterator(
            { orderHistoryBlocking(params) },
            byBitRestClient.requestIteratorCall<OrderHistoryResponse>(
                HttpMethod.Get,
                false
            )
        )

    fun ordersOpenBlocking(params: OrdersOpenParams):
            OrdersOpenResponse = runBlocking { ordersOpen(params) }

    fun ordersOpen(
        params: OrdersOpenParams,
        callback: ByBitRestApiCallback<OrdersOpenResponse>
    ) = coroutineToRestCallback(callback, { ordersOpen(params) })

    fun ordersOpenPaginated(
        params: OrdersOpenParams,
    ): RequestIterator<OrdersOpenResultItem> =
        RequestIterator(
            { ordersOpenBlocking(params) },
            byBitRestClient.requestIteratorCall<OrdersOpenResponse>(
                HttpMethod.Get,
                false
            )
        )


}
