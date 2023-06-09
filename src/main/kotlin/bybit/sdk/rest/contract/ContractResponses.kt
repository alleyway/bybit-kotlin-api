package bybit.sdk.rest.contract

import bybit.sdk.rest.ByBitRestOption
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@SafeVarargs
suspend fun ByBitContractClient.getServerTime(
	vararg opts: ByBitRestOption
): ServerTimeDTO =
	byBitRestClient.fetchResult({
		path("v3", "public", "time")
	}, *opts)


@Serializable
sealed class APIResponseV3(
	@SerialName("retCode") val retCode: Int = 0,
	@SerialName("retMsg") val retMsg: String = "OK",
//	val result: String = "OK",
)

@Serializable
data class ServerTimeResult(
	val timeNano: Long,
	val timeSecond: Long
)

@Serializable
 class ServerTimeDTO(
	val retCode: Int = 0,
	val retMsg: String = "OK",
//	val retExtInfo: Object = {},
	val result: ServerTimeResult

	)
