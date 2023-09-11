package bybit.sdk.websocket


/**
 * Returns a wrapped version of a function that automatically handles an uncaught exception or error. In case of an uncaught exception or error, the return will be null.
 *
 *  [Concurrency] already use this to wrap nearly everything that can happen during the lifespan of the application.
 * Therefore, it usually shouldn't be necessary to manually use this.
 */
fun <R> (() -> R).wrapCrashHandling(
): () -> R?
        = {
    try {
        this()
    } catch (e: Throwable) {
        println("uncaught Throwable: ${e.message}")
        null
    }
}

/**
 * Returns a wrapped version of a function that automatically handles an uncaught exception or error.
 *
 * [Concurrency] already use this to wrap nearly everything that can happen during the lifespan of the application.
 * Therefore, it usually shouldn't be necessary to manually use this.
 */
fun (() -> Unit).wrapCrashHandlingUnit(): () -> Unit {
    val wrappedReturning = this.wrapCrashHandling()
    // Don't instantiate a new lambda every time the return get called.
    return { wrappedReturning() ?: Unit }
}
