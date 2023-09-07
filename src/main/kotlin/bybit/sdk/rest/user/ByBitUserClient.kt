package bybit.sdk.rest.user

import bybit.sdk.ext.coroutineToRestCallback
import bybit.sdk.rest.ByBitRestApiCallback
import bybit.sdk.rest.ByBitRestClient
import kotlinx.coroutines.runBlocking

class ByBitUserClient
internal constructor(internal val byBitRestClient: ByBitRestClient) {

    fun getKeyInformationBlocking():
            KeyInformationResponse = runBlocking { getKeyInformation() }

    /** See [getKeyInformationBlocking] */

    fun getKeyInformation(
        callback: ByBitRestApiCallback<KeyInformationResponse>
    ) = coroutineToRestCallback(callback, { getKeyInformation() })


    fun modifySubKeyBlocking(params: ModifySubKeyParams):
            ModifySubKeyResponse = runBlocking { modifySubKey(params) }

    /** See [modifySubKeyBlocking] */

    fun modifySubKey(
        params: ModifySubKeyParams,
        callback: ByBitRestApiCallback<ModifySubKeyResponse>
    ) = coroutineToRestCallback(callback, { modifySubKey(params) })
}
