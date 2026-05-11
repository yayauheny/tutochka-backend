package yayauheny.by.unit.util

import io.ktor.http.Parameters
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import yayauheny.by.util.toPaginationRequest

@DisplayName("Pagination Edge Case Tests")
class PaginationEdgeCaseTest {
    private fun createQueryParameters(queryParams: Map<String, String>): Parameters =
        Parameters.build {
            queryParams.forEach { (key, value) ->
                append(key, value)
            }
        }

    @Nested
    @DisplayName("Page and Size Defaults")
    inner class PageAndSizeDefaults {
        @Test
        @DisplayName("Negative page clamped to 0, invalid string defaults to 0")
        fun page_defaults_and_clamping() {
            assertEquals(0, createQueryParameters(mapOf("page" to "-1", "size" to "10")).toPaginationRequest().page)
            assertEquals(0, createQueryParameters(mapOf("page" to "abc", "size" to "10")).toPaginationRequest().page)
            assertEquals(0, createQueryParameters(mapOf("size" to "10")).toPaginationRequest().page)
        }

        @Test
        @DisplayName("Size clamped to maxSize, negative to 1, invalid to default")
        fun size_clamping_and_defaults() {
            assertEquals(100, createQueryParameters(mapOf("page" to "0", "size" to "1000")).toPaginationRequest(maxSize = 100).size)
            assertEquals(1, createQueryParameters(mapOf("page" to "0", "size" to "-5")).toPaginationRequest().size)
            assertEquals(10, createQueryParameters(mapOf("page" to "0", "size" to "abc")).toPaginationRequest(defaultSize = 10).size)
            assertEquals(20, createQueryParameters(mapOf("page" to "0")).toPaginationRequest(defaultSize = 20).size)
        }

        @Test
        @DisplayName("No query parameters uses defaults")
        fun empty_params_use_defaults() {
            val pagination = createQueryParameters(emptyMap()).toPaginationRequest(defaultSize = 15)
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
            val queryParameters = createQueryParameters(mapOf("filters" to "name:EQ:Test,status:EQ:ACTIVE"))
            assertEquals(2, queryParameters.toPaginationRequest().filters.size)
        }

        @Test
        @DisplayName("Invalid direction defaults to ASC")
        fun invalid_direction_defaults_to_asc() {
            val pagination = createQueryParameters(mapOf("direction" to "INVALID")).toPaginationRequest()
            assertEquals(yayauheny.by.common.query.SortDirection.ASC, pagination.direction)
        }
    }
}
