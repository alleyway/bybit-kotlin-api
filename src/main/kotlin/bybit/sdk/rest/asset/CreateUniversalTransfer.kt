package bybit.sdk.rest.asset

import bybit.sdk.rest.APIResponseV5
import bybit.sdk.shared.AccountType
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder

suspend fun ByBitAssetClient.createUniversalTransfer(
    params: CreateUniversalTransferParams
): CreateUniversalTransferResponse =
    byBitRestClient.call({
        path(
            "v5",
            "asset",
            "transfer",
            "universal-transfer",
        )

        params.transferId.let { parameters["transferId"] = it.toString() }
        params.coin.let { parameters["coin"] = it.toString() }
        params.amount.let { parameters["amount"] = it.toString() }

        params.fromMemberId.let { parameters["fromMemberId"] = it.toString() }
        params.toMemberId.let { parameters["toMemberId"] = it.toString() }

        params.fromAccountType.let { parameters["fromAccountType"] = it.toString() }
        params.toAccountType.let { parameters["toAccountType"] = it.toString() }

    }, HttpMethod.Post, false)

@Builder
data class CreateUniversalTransferParams(
    val transferId: String, // "be7a2462-1138-4e27-80b1-62653f24925e",
    val coin: String, // "ETH"
    val amount: String, // "0.4"
    val fromMemberId: Int,
    val toMemberId: Int,
    val fromAccountType: AccountType,
    val toAccountType: AccountType
)

@Serializable
data class CreateUniversalTransferResult(
     val transferId: String
)

@Serializable
data class CreateUniversalTransferResponse(
    val result: CreateUniversalTransferResult,
) : APIResponseV5()
