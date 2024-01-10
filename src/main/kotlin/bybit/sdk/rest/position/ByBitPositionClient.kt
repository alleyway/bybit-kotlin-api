package bybit.sdk.rest.position

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.APIResponseV5
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import bybit.sdk.rest.RequestIterator
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

class ByBitPositionClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    fun getPositionInfoBlocking(params: PositionInfoParams):
            PositionInfoResponse = runBlocking { getPositionInfo(params) }

    /** See [getPositionInfoBlocking] */

    fun getPositionInfo(
        params: PositionInfoParams,
        callback: ByBitRestApiCallback<PositionInfoResponse>
    ) = coroutineToRestCallback(callback, { getPositionInfo(params) })


    fun setLeverageBlocking(params: LeverageParams):
            APIResponseV5 = runBlocking { setLeverage(params) }

    /** See [setLeverageBlocking] */

    fun setLeverage(
        params: LeverageParams,
        callback: ByBitRestApiCallback<APIResponseV5>
    ) = coroutineToRestCallback(callback, { setLeverage(params) })


    fun switchIsolatedBlocking(params: SwitchIsolatedParams):
            APIResponseV5 = runBlocking { switchIsolated(params) }

    /** See [switchIsolatedBlocking] */

    fun switchIsolated(
        params: SwitchIsolatedParams,
        callback: ByBitRestApiCallback<APIResponseV5>
    ) = coroutineToRestCallback(callback, { switchIsolated(params) })

    fun closedPnLs(
        params: ClosedPnLParams,
        callback: ByBitRestApiCallback<ClosedPnLResponse>
    ) = coroutineToRestCallback(callback, { closedPnLs(params) })

    fun closedPnLsBlocking(params: ClosedPnLParams):
            ClosedPnLResponse = runBlocking { closedPnLs(params) }


    fun closedPnLsPaginated(
        params: ClosedPnLParams,
    ): RequestIterator<ClosedPnLResponseItem> =
        RequestIterator(
            { closedPnLsBlocking(params) },
            byBitRestClient.requestIteratorCall<ClosedPnLResponse>(
                HttpMethod.Get,
                false
            )
        )

}
