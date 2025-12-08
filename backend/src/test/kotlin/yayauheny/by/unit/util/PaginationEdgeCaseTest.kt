package yayauheny.by.unit.util

import io.ktor.http.Parameters
import io.ktor.server.request.ApplicationRequest
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
    @DisplayName("Page Parameter Edge Cases")
    inner class PageParameterEdgeCases {
        @Test
        @DisplayName("Negative page should be clamped to 0")
        fun negative_page_should_be_clamped_to_zero() {
            val call = createMockCall(mapOf("page" to "-1", "size" to "10"))

            val pagination = call.toPaginationRequest()

            assertEquals(0, pagination.page)
        }

        @Test
        @DisplayName("Very large page number should pass through")
        fun very_large_page_number_should_pass_through() {
            val call = createMockCall(mapOf("page" to "999999", "size" to "10"))

            val pagination = call.toPaginationRequest()

            assertEquals(999999, pagination.page)
        }

        @Test
        @DisplayName("Zero page should be valid")
        fun zero_page_should_be_valid() {
            val call = createMockCall(mapOf("page" to "0", "size" to "10"))

            val pagination = call.toPaginationRequest()

            assertEquals(0, pagination.page)
        }

        @Test
        @DisplayName("Invalid page string should default to 0")
        fun invalid_page_string_should_default_to_zero() {
            val call = createMockCall(mapOf("page" to "abc", "size" to "10"))

            val pagination = call.toPaginationRequest()

            assertEquals(0, pagination.page)
        }

        @Test
        @DisplayName("Missing page parameter should default to 0")
        fun missing_page_parameter_should_default_to_zero() {
            val call = createMockCall(mapOf("size" to "10"))

            val pagination = call.toPaginationRequest()

            assertEquals(0, pagination.page)
        }
    }

    @Nested
    @DisplayName("Size Parameter Edge Cases")
    inner class SizeParameterEdgeCases {
        @Test
        @DisplayName("Size should be clamped to maxSize")
        fun size_should_be_clamped_to_max_size() {
            val call = createMockCall(mapOf("page" to "0", "size" to "1000"))

            val pagination = call.toPaginationRequest(maxSize = 100)

            assertEquals(100, pagination.size)
        }

        @Test
        @DisplayName("Size less than 1 should be clamped to 1")
        fun size_less_than_one_should_be_clamped_to_one() {
            val call = createMockCall(mapOf("page" to "0", "size" to "0"))

            val pagination = call.toPaginationRequest()

            assertEquals(1, pagination.size)
        }

        @Test
        @DisplayName("Negative size should be clamped to 1")
        fun negative_size_should_be_clamped_to_one() {
            val call = createMockCall(mapOf("page" to "0", "size" to "-5"))

            val pagination = call.toPaginationRequest()

            assertEquals(1, pagination.size)
        }

        @Test
        @DisplayName("Invalid size string should use default")
        fun invalid_size_string_should_use_default() {
            val call = createMockCall(mapOf("page" to "0", "size" to "abc"))

            val pagination = call.toPaginationRequest(defaultSize = 10)

            assertEquals(10, pagination.size)
        }

        @Test
        @DisplayName("Missing size parameter should use default")
        fun missing_size_parameter_should_use_default() {
            val call = createMockCall(mapOf("page" to "0"))

            val pagination = call.toPaginationRequest(defaultSize = 20)

            assertEquals(20, pagination.size)
        }

        @Test
        @DisplayName("Size equal to maxSize should pass")
        fun size_equal_to_max_size_should_pass() {
            val call = createMockCall(mapOf("page" to "0", "size" to "100"))

            val pagination = call.toPaginationRequest(maxSize = 100)

            assertEquals(100, pagination.size)
        }

        @Test
        @DisplayName("Size equal to 1 should pass")
        fun size_equal_to_one_should_pass() {
            val call = createMockCall(mapOf("page" to "0", "size" to "1"))

            val pagination = call.toPaginationRequest()

            assertEquals(1, pagination.size)
        }
    }

    @Nested
    @DisplayName("Empty Query Parameters")
    inner class EmptyQueryParameters {
        @Test
        @DisplayName("No query parameters should use defaults")
        fun no_query_parameters_should_use_defaults() {
            val call = createMockCall(emptyMap())

            val pagination = call.toPaginationRequest(defaultSize = 15)

            assertEquals(0, pagination.page)
            assertEquals(15, pagination.size)
            assertEquals(emptyList(), pagination.filters)
        }
    }

    @Nested
    @DisplayName("Filter Edge Cases")
    inner class FilterEdgeCases {
        @Test
        @DisplayName("Empty filters parameter should return empty list")
        fun empty_filters_parameter_should_return_empty_list() {
            val call = createMockCall(mapOf("filters" to ""))

            val pagination = call.toPaginationRequest()

            assertEquals(emptyList(), pagination.filters)
        }

        @Test
        @DisplayName("Invalid filter format should be ignored")
        fun invalid_filter_format_should_be_ignored() {
            val call = createMockCall(mapOf("filters" to "invalid"))

            val pagination = call.toPaginationRequest()

            assertEquals(emptyList(), pagination.filters)
        }

        @Test
        @DisplayName("Filter with invalid operator should be ignored")
        fun filter_with_invalid_operator_should_be_ignored() {
            val call = createMockCall(mapOf("filters" to "name:INVALID:value"))

            val pagination = call.toPaginationRequest()

            assertEquals(emptyList(), pagination.filters)
        }

        @Test
        @DisplayName("Multiple filters should be parsed correctly")
        fun multiple_filters_should_be_parsed_correctly() {
            val call = createMockCall(mapOf("filters" to "name:EQ:Test,status:EQ:ACTIVE"))

            val pagination = call.toPaginationRequest()

            assertEquals(2, pagination.filters.size)
        }
    }

    @Nested
    @DisplayName("Sort Direction Edge Cases")
    inner class SortDirectionEdgeCases {
        @Test
        @DisplayName("Invalid direction should default to ASC")
        fun invalid_direction_should_default_to_asc() {
            val call = createMockCall(mapOf("direction" to "INVALID"))

            val pagination = call.toPaginationRequest()

            assertEquals(yayauheny.by.common.query.SortDirection.ASC, pagination.direction)
        }

        @Test
        @DisplayName("Lowercase direction should be converted to uppercase")
        fun lowercase_direction_should_be_converted_to_uppercase() {
            val call = createMockCall(mapOf("direction" to "desc"))

            val pagination = call.toPaginationRequest()

            assertEquals(yayauheny.by.common.query.SortDirection.DESC, pagination.direction)
        }

        @Test
        @DisplayName("Missing direction should default to ASC")
        fun missing_direction_should_default_to_asc() {
            val call = createMockCall(mapOf("page" to "0"))

            val pagination = call.toPaginationRequest()

            assertEquals(yayauheny.by.common.query.SortDirection.ASC, pagination.direction)
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    inner class BoundaryValueTests {
        @ParameterizedTest
        @CsvSource(
            "0, 1",
            "0, 100",
            "999999, 1",
            "999999, 100"
        )
        @DisplayName("Boundary page and size combinations should work")
        fun boundary_page_and_size_combinations_should_work(
            page: Int,
            size: Int
        ) {
            val call = createMockCall(mapOf("page" to page.toString(), "size" to size.toString()))

            val pagination = call.toPaginationRequest(maxSize = 100)

            assertEquals(page.coerceAtLeast(0), pagination.page)
            assertEquals(size.coerceIn(1, 100), pagination.size)
        }
    }
}
