package toshibaac.cli

internal interface SuspendLazy<T : Any> {
    suspend fun get(): T
}

internal class LazyCloseable<C : AutoCloseable>(
    private val construct: suspend () -> C,
) : AutoCloseable,
    SuspendLazy<C> {
    private var innerCloseable: C? = null

    override suspend fun get(): C = innerCloseable ?: construct().also { innerCloseable = it }

    override fun close() {
        innerCloseable?.close()
    }
}
