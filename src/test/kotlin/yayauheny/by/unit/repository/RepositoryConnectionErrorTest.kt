package unit.repository

import io.mockk.mockk
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.repository.impl.RestroomRepositoryImpl

@DisplayName("Repository Query Execution Error Tests")
class RepositoryConnectionErrorTest {
    private val mockDslContext = mockk<DSLContext>(relaxed = true)

    @Nested
    @DisplayName("Invalid Query Parameters Tests")
    inner class InvalidQueryParametersTests {
        @Test
        @DisplayName("GIVEN invalid latitude WHEN findNearestByLocation THEN throw IllegalArgumentException")
        fun given_invalid_latitude_when_find_nearest_by_location_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findNearestByLocation(
                        latitude = 200.0,
                        longitude = 37.6176,
                        limit = 10,
                        distanceMeters = 1000
                    )
                }
            }

        @Test
        @DisplayName("GIVEN invalid longitude WHEN findNearestByLocation THEN throw IllegalArgumentException")
        fun given_invalid_longitude_when_find_nearest_by_location_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findNearestByLocation(
                        latitude = 55.7558,
                        longitude = -200.0,
                        limit = 10,
                        distanceMeters = 1000
                    )
                }
            }

        @Test
        @DisplayName("GIVEN negative distanceMeters WHEN findNearestByLocation THEN throw IllegalArgumentException")
        fun given_negative_distance_meters_when_find_nearest_by_location_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findNearestByLocation(
                        latitude = 55.7558,
                        longitude = 37.6176,
                        limit = 10,
                        distanceMeters = -100
                    )
                }
            }

        @Test
        @DisplayName("GIVEN zero limit WHEN findNearestByLocation THEN throw IllegalArgumentException")
        fun given_zero_limit_when_find_nearest_by_location_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findNearestByLocation(
                        latitude = 55.7558,
                        longitude = 37.6176,
                        limit = 0,
                        distanceMeters = 1000
                    )
                }
            }

        @Test
        @DisplayName("GIVEN negative limit WHEN findNearestByLocation THEN throw IllegalArgumentException")
        fun given_negative_limit_when_find_nearest_by_location_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findNearestByLocation(
                        latitude = 55.7558,
                        longitude = 37.6176,
                        limit = -1,
                        distanceMeters = 1000
                    )
                }
            }

        @Test
        @DisplayName("GIVEN negative page WHEN findAll THEN throw IllegalArgumentException")
        fun given_negative_page_when_find_all_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findAll(
                        PaginationRequest(page = -1, size = 10)
                    )
                }
            }

        @Test
        @DisplayName("GIVEN zero size WHEN findAll THEN throw IllegalArgumentException")
        fun given_zero_size_when_find_all_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findAll(
                        PaginationRequest(page = 0, size = 0)
                    )
                }
            }

        @Test
        @DisplayName("GIVEN negative size WHEN findAll THEN throw IllegalArgumentException")
        fun given_negative_size_when_find_all_then_throw_illegal_argument_exception() =
            runTest {
                val repository = RestroomRepositoryImpl(mockDslContext)

                assertFailsWith<IllegalArgumentException> {
                    repository.findAll(
                        PaginationRequest(page = 0, size = -1)
                    )
                }
            }
    }
}
