package yayauheny.by.unit.util

import io.ktor.http.Parameters
import io.ktor.server.request.ApplicationRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.util.toPaginationRequest
import kotlin.test.assertEquals

@DisplayName("Pagination Edge Case Tests")
class PaginationEdgeCaseTest {
    private fun createMockCall(queryParams: Map<String, String>): io.ktor.server.application.ApplicationCall {
        val request = mockk<ApplicationRequest>(relaxed = true)
        val parameters =
            Parameters.build {
                queryParams.forEach { (key, value) ->
                    append(key, value)
                }
            }
        every { request.queryParameters } returns parameters

        val call = mockk<io.ktor.server.application.ApplicationCall>(relaxed = true)
        every { call.request } returns request
        return call
    }

    @Nested
    @DisplayName("Page and Size Defaults")
    inner class PageAndSizeDefaults {
        @Test
        @DisplayName("Negative page clamped to 0, invalid string defaults to 0")
        fun page_defaults_and_clamping() {
            assertEquals(0, createMockCall(mapOf("page" to "-1", "size" to "10")).toPaginationRequest().page)
            assertEquals(0, createMockCall(mapOf("page" to "abc", "size" to "10")).toPaginationRequest().page)
            assertEquals(0, createMockCall(mapOf("size" to "10")).toPaginationRequest().page)
        }

        @Test
        @DisplayName("Size clamped to maxSize, negative to 1, invalid to default")
        fun size_clamping_and_defaults() {
            assertEquals(100, createMockCall(mapOf("page" to "0", "size" to "1000")).toPaginationRequest(maxSize = 100).size)
            assertEquals(1, createMockCall(mapOf("page" to "0", "size" to "-5")).toPaginationRequest().size)
            assertEquals(10, createMockCall(mapOf("page" to "0", "size" to "abc")).toPaginationRequest(defaultSize = 10).size)
            assertEquals(20, createMockCall(mapOf("page" to "0")).toPaginationRequest(defaultSize = 20).size)
        }

        @Test
        @DisplayName("No query parameters uses defaults")
        fun empty_params_use_defaults() {
            val pagination = createMockCall(emptyMap()).toPaginationRequest(defaultSize = 15)
            assertEquals(0, pagination.page)
            assertEquals(15, pagination.size)
            assertEquals(emptyList(), pagination.filters)
        }
    }

    @Nested
    @DisplayName("Filters and Sort")
    inner class FiltersAndSort {
        @Test
        @DisplayName("Multiple filters parsed correctly")
        fun multiple_filters_parsed() {
            val call = createMockCall(mapOf("filters" to "name:EQ:Test,status:EQ:ACTIVE"))
            assertEquals(2, call.toPaginationRequest().filters.size)
        }

        @Test
        @DisplayName("Invalid direction defaults to ASC")
        fun invalid_direction_defaults_to_asc() {
            val pagination = createMockCall(mapOf("direction" to "INVALID")).toPaginationRequest()
            assertEquals(yayauheny.by.common.query.SortDirection.ASC, pagination.direction)
        }
    }
}
