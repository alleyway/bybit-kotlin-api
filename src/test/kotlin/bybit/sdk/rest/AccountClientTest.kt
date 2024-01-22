package bybit.sdk.rest


import bybit.sdk.properties.ByBitProperties
import bybit.sdk.rest.account.FeeRateParams
import bybit.sdk.rest.account.WalletBalanceParams
import bybit.sdk.shared.AccountType
import bybit.sdk.shared.Category
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AccountClientTest {

    private var restClient: ByBitRestClient =
        ByBitRestClient(
            apiKey = System.getenv("BYBIT_API_KEY") ?: ByBitProperties.APIKEY,
            secret = System.getenv("BYBIT_SECRET") ?: ByBitProperties.SECRET,
            testnet = true,
//            httpClientProvider = okHttpClientProvider
        )

    @Test
    fun getWalletBalanceTest() {
        val resp = restClient.accountClient.getWalletBalanceBlocking(
            WalletBalanceParams(
                accountType = AccountType.SPOT
            )
        )
        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)
    }

    @Test
    fun getFeeRateTest() {
        val resp = restClient.accountClient.getFeeRateBlocking(
            FeeRateParams(
                category = Category.inverse,
                symbol = "BTCUSD"
            )
        )
        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)

        assertEquals("BTCUSD", resp.result.list.first().symbol)

    }


}
