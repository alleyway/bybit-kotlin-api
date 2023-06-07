package bybit.sdk.rest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class APIResponse(

	@SerialName("ret_code") val retCode: Int = 0,
	@SerialName("ret_msg") val retMsg: String = "OK",

	@SerialName("ext_code") val extCode: String?,
	@SerialName("ext_info") val extInfo: String?,

//	result: T;
)
