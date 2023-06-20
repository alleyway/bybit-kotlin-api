package bybit.sdk.shared

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    CONTRACT,
    SPOT,
    OPTION,
    UNIFIED,
    FUND
}
