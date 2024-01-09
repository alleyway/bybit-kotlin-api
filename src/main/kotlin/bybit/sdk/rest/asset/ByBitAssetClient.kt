package bybit.sdk.rest.asset

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import kotlinx.coroutines.runBlocking

class ByBitAssetClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    fun createUniversalTransferBlocking(params: CreateUniversalTransferParams):
            CreateUniversalTransferResponse = runBlocking { createUniversalTransfer(params) }

    fun createUniversalTransfer(
        params: CreateUniversalTransferParams,
        callback: ByBitRestApiCallback<CreateUniversalTransferResponse>
    ) = coroutineToRestCallback(callback, { createUniversalTransfer(params) })


}
