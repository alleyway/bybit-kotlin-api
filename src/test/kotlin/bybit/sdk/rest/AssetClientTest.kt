package bybit.sdk.rest


import bybit.sdk.properties.ByBitProperties
import bybit.sdk.rest.asset.CreateUniversalTransferParams
import bybit.sdk.shared.AccountType
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AssetClientTest {

    private var restClient: ByBitRestClient =
        ByBitRestClient(
            apiKey = System.getenv("BYBIT_API_KEY") ?: ByBitProperties.APIKEY,
            secret = System.getenv("BYBIT_SECRET") ?: ByBitProperties.SECRET,
            testnet = true,
//            httpClientProvider = okHttpClientProvider
        )

    @Test
    fun createUniversalTransfer() {
        val resp = restClient.assetClient.createUniversalTransferBlocking(
            CreateUniversalTransferParams(
                transferId = UUID.randomUUID().toString(),
                coin = "BTC",
                amount = "0.001",
                fromMemberId = "100488137",
                toMemberId = "408568",
                fromAccountType = AccountType.CONTRACT,
                toAccountType = AccountType.CONTRACT
            )
        )
        assertEquals(0, resp.retCode)
        assertEquals("success", resp.retMsg)
    }
}
