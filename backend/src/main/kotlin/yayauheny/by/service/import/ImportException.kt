package yayauheny.by.service.import

import java.util.UUID
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

sealed class ImportException(
    message: String
) : RuntimeException(message)

class UnsupportedImportProvider(
    provider: String
) : ImportException("Unsupported import provider: $provider")

class UnsupportedPayloadType(
    val provider: ImportProvider,
    val payloadType: ImportPayloadType
) : ImportException("Unsupported payloadType=$payloadType for provider=$provider")

class CityNotFound(
    cityId: UUID
) : ImportException("City not found: $cityId")

class InvalidImportPayload(
    message: String
) : ImportException(message)
