package yayauheny.by.importing.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ImportCityResolver {
    
    sealed class MetadataCityResolution {
        data class Resolved(val cityId: UUID) : MetadataCityResolution()
        data class NeedsNearestFallback(val countryId: UUID?, val reason: String) : MetadataCityResolution()
    }

    fun resolveByMetadata(countryName: String?, cityName: String?): MetadataCityResolution {
        // TODO: Rewrite city resolution logic with JPA
        return MetadataCityResolution.NeedsNearestFallback(null, "Not implemented yet")
    }

    fun resolveNearest(lat: Double?, lng: Double?, countryId: UUID?): UUID? {
        // TODO: Rewrite spatial query using JPA
        return null
    }
}
