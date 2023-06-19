package bybit.sdk.rest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
open class APIResponseV5(
	val retCode: Int = 0,
	val retMsg: String = "OK",
	val time: Long = 0
)

@Serializable
data class APIResponse(

	@SerialName("ret_code") val retCode: Int = 0,
	@SerialName("ret_msg") val retMsg: String = "OK",

	@SerialName("ext_code") val extCode: String?,
	@SerialName("ext_info") val extInfo: String?,

//	result: T;
)
