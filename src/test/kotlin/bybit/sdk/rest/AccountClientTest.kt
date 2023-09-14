package bybit.sdk.rest


import bybit.sdk.rest.account.WalletBalanceParams
import bybit.sdk.shared.AccountType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AccountClientTest {

    private var restClient: ByBitRestClient =
        ByBitRestClient(
            apiKey = System.getenv("BYBIT_API_KEY"),
            secret = System.getenv("BYBIT_SECRET"),
            testnet = true,
            httpClientProvider = okHttpClientProvider)

    @Test
    fun getWalletBalance() {
        val resp = restClient.accountClient.getWalletBalanceBlocking(
            WalletBalanceParams(
                accountType = AccountType.SPOT
            )
        )
        assertEquals(0, resp.retCode)
        assertEquals("OK", resp.retMsg)
    }
}
