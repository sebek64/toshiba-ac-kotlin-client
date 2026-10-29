package toshibaac.client.http

public class HttpStatusException internal constructor(
    public val statusCode: Int,
    public val body: String,
) : RuntimeException("HTTP request failed with status code $statusCode: $body")
