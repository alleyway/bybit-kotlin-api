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

    fun cancelOrderBlocking(params: CancelOrderParams):
            CancelOrderResponse = runBlocking { cancelOrder(params) }

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

    @SafeVarargs
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

}
