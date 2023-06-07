package bybit.sdk.rest

interface ByBitRestApiCallback<T> {
    fun onSuccess(result: T)
    fun onError(error: Throwable)
}
