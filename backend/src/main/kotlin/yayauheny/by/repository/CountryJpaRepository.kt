package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yayauheny.by.entity.CountryEntity
import java.util.UUID

@Repository
interface CountryJpaRepository : JpaRepository<CountryEntity, UUID>, CountryFilterRepository {
    fun findByCode(code: String): CountryEntity?
}
