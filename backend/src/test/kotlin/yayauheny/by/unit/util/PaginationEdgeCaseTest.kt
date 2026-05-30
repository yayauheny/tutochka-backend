package yayauheny.by.unit.util

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import yayauheny.by.util.toPaginationRequest

@DisplayName("Pagination Edge Case Tests")
class PaginationEdgeCaseTest {
    private fun createRequest(queryParams: Map<String, String>): HttpServletRequest =
        mockk {
            every { getParameter(any()) } answers { queryParams[args[0] as String] }
        }

    @Nested
    @DisplayName("Page and Size Defaults")
    inner class PageAndSizeDefaults {
        @Test
        @DisplayName("Negative page clamped to 0, invalid string defaults to 0")
        fun page_defaults_and_clamping() {
            assertEquals(0, createRequest(mapOf("page" to "-1", "size" to "10")).toPaginationRequest().page)
            assertEquals(0, createRequest(mapOf("page" to "abc", "size" to "10")).toPaginationRequest().page)
            assertEquals(0, createRequest(mapOf("size" to "10")).toPaginationRequest().page)
        }

        @Test
        @DisplayName("Size clamped to maxSize, negative to 1, invalid to default")
        fun size_clamping_and_defaults() {
            assertEquals(100, createRequest(mapOf("page" to "0", "size" to "1000")).toPaginationRequest(maxSize = 100).size)
            assertEquals(1, createRequest(mapOf("page" to "0", "size" to "-5")).toPaginationRequest().size)
            assertEquals(10, createRequest(mapOf("page" to "0", "size" to "abc")).toPaginationRequest(defaultSize = 10).size)
            assertEquals(20, createRequest(mapOf("page" to "0")).toPaginationRequest(defaultSize = 20).size)
        }

        @Test
        @DisplayName("No query parameters uses defaults")
        fun empty_params_use_defaults() {
            val pagination = createRequest(emptyMap()).toPaginationRequest(defaultSize = 15)
            assertEquals(0, pagination.page)
            assertEquals(15, pagination.size)
            assertNull(pagination.sort)
        }
    }

    @Nested
    @DisplayName("Filters and Sort")
    inner class FiltersAndSort {
        @Test
        @DisplayName("Invalid direction defaults to ASC")
        fun invalid_direction_defaults_to_asc() {
            val pagination = createRequest(mapOf("direction" to "INVALID")).toPaginationRequest()
            assertEquals(yayauheny.by.common.query.SortDirection.ASC, pagination.direction)
        }
    }
}
