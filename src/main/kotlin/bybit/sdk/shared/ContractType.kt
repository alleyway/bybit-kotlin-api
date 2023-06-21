package bybit.sdk.shared

import kotlinx.serialization.Serializable

@Serializable
enum class ContractType {
    InversePerpetual,
    LinearPerpetual,
    LinearFutures,
    InverseFutures,
}
