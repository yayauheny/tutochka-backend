package integration.base

import yayauheny.by.helpers.MockMvcResponse
import yayauheny.by.helpers.testDelete
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.helpers.testPut

object KtorTestApplication {
    fun withApp(
        testDslContext: Any?,
        block: (SpringTestClient) -> Unit
    ) {
        block(SpringTestClient(IntegrationTestContext.mockMvc))
    }
}

class SpringTestClient internal constructor(
    private val mockMvc: org.springframework.test.web.servlet.MockMvc
) {
    fun testGet(
        path: String,
        query: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): MockMvcResponse = mockMvc.testGet(path, query, headers)

    fun testPost(
        path: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap()
    ): MockMvcResponse = mockMvc.testPost(path, jsonBody, headers)

    fun testPut(
        path: String,
        jsonBody: String
    ): MockMvcResponse = mockMvc.testPut(path, jsonBody)

    fun testDelete(path: String): MockMvcResponse = mockMvc.testDelete(path)

    fun get(
        path: String,
        query: Map<String, String> = emptyMap()
    ): MockMvcResponse = mockMvc.testGet(path, query)

    fun get(
        path: String,
        block: GetRequestBuilder.() -> Unit
    ): MockMvcResponse {
        val builder = GetRequestBuilder().apply(block)
        return mockMvc.testGet(path, builder.query, builder.headers.toMap())
    }

    fun post(
        path: String,
        body: String,
        headers: Map<String, String> = emptyMap()
    ): MockMvcResponse = mockMvc.testPost(path, body, headers)

    fun post(
        path: String,
        block: PostRequestBuilder.() -> Unit
    ): MockMvcResponse {
        val builder = PostRequestBuilder().apply(block)
        return mockMvc.testPost(path, builder.requestBody, builder.headers.toMap())
    }

    fun put(
        path: String,
        body: String
    ): MockMvcResponse = mockMvc.testPut(path, body)

    fun delete(path: String): MockMvcResponse = mockMvc.testDelete(path)
}

class PostRequestBuilder {
    var requestBody: String = ""
    private val headersMap: MutableMap<String, String> = linkedMapOf()
    val headers = HeaderRequestBuilder(headersMap)
    private val urlBuilder = UrlRequestBuilder()

    fun contentType(value: Any) {
        @Suppress("UNUSED_VARIABLE")
        val ignored = value
    }

    fun setBody(value: String) {
        requestBody = value
    }

    fun header(
        name: String,
        value: String
    ) {
        headersMap[name] = value
    }

    fun url(block: UrlRequestBuilder.() -> Unit) {
        urlBuilder.block()
    }
}

class GetRequestBuilder {
    private val queryMap = linkedMapOf<String, String>()
    val query: MutableMap<String, String> = queryMap
    private val headersMap = linkedMapOf<String, String>()
    val headers = HeaderRequestBuilder(headersMap)
    private val urlBuilder = UrlRequestBuilder(query)

    fun url(block: UrlRequestBuilder.() -> Unit) {
        urlBuilder.block()
    }
}

class HeaderRequestBuilder(
    private val target: MutableMap<String, String>
) {
    fun append(
        name: String,
        value: String
    ) {
        target[name] = value
    }

    fun toMap(): Map<String, String> = target
}

class UrlRequestBuilder(
    private val target: MutableMap<String, String> = linkedMapOf()
) {
    val parameters = HeaderRequestBuilder(target)
}
