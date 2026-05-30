package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yayauheny.by.entity.CityEntity
import java.util.UUID

@Repository
interface CityJpaRepository : JpaRepository<CityEntity, UUID>, CityFilterRepository {
    fun findByCountryIdAndNameRu(countryId: UUID, nameRu: String): CityEntity?
    fun findByCountryIdAndNameEn(countryId: UUID, nameEn: String): CityEntity?
}
