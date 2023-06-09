package bybit.sdk.rest

import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * Paginatable describes a type that supports pagination by providing a next_url attribute.
 */


interface ListResult<T> {
    val category: String
    val list: List<T>
    val nextPageCursor: String?
}

interface Paginatable<T> {
    val result: ListResult<T>?
    var nextUrl: String?
}

class IteratorExhaustedException : Exception("Iterator is out of elements to return")

/**
 * RequestIterator implements Iterator and handles paging through multiple API requests to get all the data in the resultset.
 *
 * You must call `hasNext()` at least once before calling `next()`.
 */
class RequestIterator<T>
internal constructor(
    private val firstPageFetcher: () -> Paginatable<T>,
    private val nextPageFetcher: (url: String) -> Paginatable<T>
) : Iterator<T> {

    var currentPage: Paginatable<T>? = null

    private var index = -1 // Start at -1 because the first call to `hasNext` increments index
    private var done = false

    /**
     * Return this iterator as a stream for better Java interop.
     */
    fun asStream(): Stream<T> {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED), false)
    }

    override fun hasNext(): Boolean {
        if (done) {
            return false
        }

        // If index == -1, this is the first time this function is called so we have to get the first page
        if (index == -1) {
            currentPage = firstPageFetcher()
        }

        // Increment the current index and then check if we have enough results in the current page for it to be valid.
        index++

        if (index < (currentPage?.result?.list?.size ?: -1)) {
            return true
        }

        // If we're out of results in the current page, go get the next one
        currentPage?.nextUrl?.let { nextURL ->

            if (nextURL.isEmpty()) {
                done = true
                return false
            }

            currentPage = nextPageFetcher(nextURL)
            index = 0

            if ((currentPage?.result?.list?.size ?: 0) == 0) {
                done = true
                return false
            }

            return true
        } ?: run {
            done = true
            return false
        }
    }

    @Throws(IteratorExhaustedException::class)
    override fun next(): T {
        if (done) {
            throw IteratorExhaustedException()
        }

        return currentPage?.result?.list?.get(index) ?: throw IteratorExhaustedException()
    }

}
