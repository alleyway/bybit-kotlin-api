package bybit.sdk.rest


import bybit.sdk.rest.account.WalletBalanceParams
import bybit.sdk.shared.AccountType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AccountClientTest {
    private var client: ByBitRestClient = ByBitRestClient(httpClientProvider = okHttpClientProvider)


    @Test
    fun getWalletBalance() {

        val resp = client.accountClient.getWalletBalanceBlocking(
            WalletBalanceParams(
                accountType = AccountType.SPOT
            )
        )
		assertEquals(0, resp.retCode)
		assertEquals("OK", resp.retMsg)
    }


}
