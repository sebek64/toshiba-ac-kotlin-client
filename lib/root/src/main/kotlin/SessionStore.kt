package toshibaac.client

/**
 * Persists the serialized login session between runs, so that a restart can skip the rate-limited login.
 */
public interface SessionStore {
    public suspend fun load(): String?

    public suspend fun save(session: String)
}
