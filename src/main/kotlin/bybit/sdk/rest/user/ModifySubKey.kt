package bybit.sdk.rest.user

import bybit.sdk.rest.APIResponseV5
import io.ktor.http.*
import kotlinx.serialization.Serializable
import lombok.Builder

suspend fun ByBitUserClient.modifySubKey(
    params: ModifySubKeyParams
): ModifySubKeyResponse =
    byBitRestClient.call(
        {
        path(
            "v5",
            "user",
            "update-sub-api",
        )
            params.readOnly?.let { parameters["readOnly"] = it.toString() }
            params.ips?.let {
                parameters["ips"] = it.joinToString(prefix = "[", postfix = "]")
            }

        }, HttpMethod.Post, false)

@Builder
data class ModifySubKeyParams(
    val readOnly: Int?,
    val ips: List<String>? = null,
)

@Serializable
data class ModifySubKeyResult(
    val id: String,
    val note: String,
    val apiKey: String,
    val readOnly: Int,
    val secret: String,
    val ips: List<String>,
)

@Serializable
data class ModifySubKeyResponse(
    val result: ModifySubKeyResult,
) : APIResponseV5()
