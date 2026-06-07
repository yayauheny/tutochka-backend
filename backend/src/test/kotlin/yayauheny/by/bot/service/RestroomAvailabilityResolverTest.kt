package yayauheny.by.bot.service

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.service.import.schedule.OsmScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.schedule.YandexScheduleAdapter

class RestroomAvailabilityResolverTest {
    @Test
    fun `resolve returns open for active 2gis schedule`() {
        val resolver = resolverAt(Instant.parse("2024-12-30T09:00:00Z"))

        val resolved = resolver.resolve(sampleRestroom(workTime = sampleTwoGisSchedule()))

        assertThat(resolved.status).isEqualTo(AvailabilityState.OPEN)
        assertThat(resolved.statusText).isEqualTo("🟢 Туалет открыт сейчас")
        assertThat(resolved.workTimeText).contains("Пн: 09:00-18:00")
    }

    @Test
    fun `resolve returns closed for inactive 2gis schedule`() {
        val resolver = resolverAt(Instant.parse("2024-12-30T20:00:00Z"))

        val resolved = resolver.resolve(sampleRestroom(workTime = sampleTwoGisSchedule()))

        assertThat(resolved.status).isEqualTo(AvailabilityState.CLOSED)
        assertThat(resolved.statusText).isEqualTo("🔴 Туалет закрыт")
    }

    @Test
    fun `resolve handles overnight intervals`() {
        val resolver = resolverAt(Instant.parse("2024-12-30T22:30:00Z"))
        val workTime = mapOf(
            "Mon" to mapOf(
                "working_hours" to listOf(
                    mapOf("from" to "22:00", "to" to "02:00")
                )
            )
        )

        val resolved = resolver.resolve(sampleRestroom(workTime = workTime))

        assertThat(resolved.status).isEqualTo(AvailabilityState.OPEN)
    }

    @Test
    fun `resolve handles twenty four seven schedule`() {
        val resolver = resolverAt(Instant.parse("2024-12-30T22:30:00Z"))

        val resolved = resolver.resolve(sampleRestroom(workTime = mapOf("is_24x7" to true)))

        assertThat(resolved.status).isEqualTo(AvailabilityState.OPEN)
        assertThat(resolved.workTimeText).isEqualTo("Круглосуточно")
    }

    @Test
    fun `resolve uses inherited building schedule`() {
        val resolver = resolverAt(Instant.parse("2024-12-30T09:00:00Z"))
        val restroom = sampleRestroom(workTime = null).copy(
            inheritBuildingSchedule = true,
            building = sampleBuilding(workTime = sampleTwoGisSchedule())
        )

        val resolved = resolver.resolve(restroom)

        assertThat(resolved.status).isEqualTo(AvailabilityState.OPEN)
    }

    @Test
    fun `resolve returns unknown for unsupported provider shape`() {
        val resolver = resolverAt(Instant.parse("2024-12-30T09:00:00Z"))
        val restroom = sampleRestroom(
            originProvider = ImportProvider.OSM,
            workTime = mapOf("opening_hours" to "Mo-Fr 09:00-18:00")
        )

        val resolved = resolver.resolve(restroom)

        assertThat(resolved.status).isEqualTo(AvailabilityState.UNKNOWN)
        assertThat(resolved.statusText).isEqualTo("🟡 Время работы неизвестно")
        assertThat(resolved.workTimeText).isEqualTo("Mo-Fr 09:00-18:00")
    }

    private fun resolverAt(now: Instant): RestroomAvailabilityResolver {
        return RestroomAvailabilityResolver(
            ScheduleMappingService(listOf(TwoGisScheduleAdapter(), YandexScheduleAdapter(), OsmScheduleAdapter())),
            Clock.fixed(now, ZoneOffset.UTC)
        )
    }

    private fun sampleRestroom(
        originProvider: ImportProvider = ImportProvider.TWO_GIS,
        workTime: Map<String, Any>? = emptyMap()
    ): RestroomResponseDto {
        val now = Instant.parse("2025-01-01T00:00:00Z")
        return RestroomResponseDto(
            id = UUID.randomUUID(),
            cityId = UUID.randomUUID(),
            cityName = "Minsk",
            buildingId = UUID.randomUUID(),
            subwayStationId = null,
            name = "Restroom",
            address = "Address",
            phones = emptyMap(),
            workTime = workTime,
            feeType = FeeType.FREE,
            genderType = null,
            accessibilityType = AccessibilityType.UNKNOWN,
            placeType = PlaceType.OTHER,
            coordinates = Coordinates(53.9, 27.56),
            dataSource = DataSourceType.USER,
            status = RestroomStatus.ACTIVE,
            amenities = emptyMap(),
            externalMaps = emptyMap(),
            accessNote = null,
            directionGuide = null,
            inheritBuildingSchedule = false,
            hasPhotos = false,
            locationType = LocationType.UNKNOWN,
            originProvider = originProvider,
            originId = null,
            isHidden = false,
            createdAt = now,
            updatedAt = now,
            distanceMeters = null,
            building = sampleBuilding(),
            subwayStation = null
        )
    }

    private fun sampleBuilding(workTime: Map<String, Any>? = emptyMap()) =
        BuildingResponseDto(
            id = UUID.randomUUID(),
            cityId = UUID.randomUUID(),
            name = "Building",
            address = "Building address",
            buildingType = PlaceType.OTHER,
            workTime = workTime,
            coordinates = Coordinates(53.91, 27.57),
            externalIds = emptyMap(),
            isDeleted = false,
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2025-01-01T00:00:00Z")
        )

    private fun sampleTwoGisSchedule(): Map<String, Any> {
        return mapOf(
            "Mon" to mapOf(
                "working_hours" to listOf(
                    mapOf(
                        "from" to "09:00",
                        "to" to "18:00"
                    )
                )
            )
        )
    }
}
