package yayauheny.by.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yayauheny.by.entity.*
import java.util.UUID

@Repository
interface CountryJpaRepository : JpaRepository<CountryEntity, UUID> {
    fun findByCode(code: String): CountryEntity?
}

@Repository
interface CityJpaRepository : JpaRepository<CityEntity, UUID> {
    fun findByCountryId(countryId: UUID, pageable: Pageable): Page<CityEntity>
    fun findByNameRuContainingIgnoreCaseOrNameEnContainingIgnoreCase(nameRu: String, nameEn: String, pageable: Pageable): Page<CityEntity>
    fun findByCountryIdAndNameRu(countryId: UUID, nameRu: String): CityEntity?
    fun findByCountryIdAndNameEn(countryId: UUID, nameEn: String): CityEntity?
}

@Repository
interface BuildingJpaRepository : JpaRepository<BuildingEntity, UUID>

@Repository
interface SubwayLineJpaRepository : JpaRepository<SubwayLineEntity, UUID> {
    fun findByCityId(cityId: UUID): List<SubwayLineEntity>
}

@Repository
interface SubwayStationJpaRepository : JpaRepository<SubwayStationEntity, UUID> {
    fun findBySubwayLineId(lineId: UUID): List<SubwayStationEntity>
    
    @Query(value = "SELECT * FROM subway_stations ORDER BY coordinates <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) LIMIT 1", nativeQuery = true)
    fun findNearestStation(lon: Double, lat: Double): SubwayStationEntity?
}

@Repository
interface RestroomJpaRepository : JpaRepository<RestroomEntity, UUID> {
    fun findByCityId(cityId: UUID, pageable: Pageable): Page<RestroomEntity>
    
    @Query(value = "SELECT * FROM restrooms WHERE ST_DWithin(coordinates, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :distanceMeters, true) ORDER BY coordinates <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) LIMIT :limit", nativeQuery = true)
    fun findNearest(lon: Double, lat: Double, distanceMeters: Int, limit: Int): List<RestroomEntity>
}

@Repository
interface RestroomImportJpaRepository : JpaRepository<RestroomImportEntity, UUID>
