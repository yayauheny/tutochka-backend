package yayauheny.by.importing.api

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import yayauheny.by.importing.service.ImportService
import yayauheny.by.common.errors.BadRequestException
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.ImportBatchResponseDto
import yayauheny.by.model.imports.ImportItemResultDto
import yayauheny.by.model.imports.ImportResponseDto
import java.util.UUID

@RestController
@RequestMapping("/api/v1/import")
class ImportController(
    private val importService: ImportService
) {
    @PostMapping
    fun importSingle(
        headers: HttpHeaders,
        @RequestBody body: JsonNode
    ): ResponseEntity<ImportResponseDto> {
        val parsedProvider = headers.importProvider()
        val parsedPayloadType = headers.importPayloadType()
        val parsedCityId = headers.importCityId()

        val payload = if (body.has("items") && body.get("items").isArray) {
            body.get("items").first()
        } else {
            body
        }

        val result = importService.import(
            provider = parsedProvider,
            payloadType = parsedPayloadType,
            cityId = parsedCityId,
            payload = payload
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ImportResponseDto(
                importId = result.importId,
                restroomId = result.restroomId,
                buildingId = result.buildingId,
                status = result.status
            )
        )
    }

    @PostMapping("/batch")
    fun importBatch(
        headers: HttpHeaders,
        @RequestBody body: JsonNode
    ): ResponseEntity<ImportBatchResponseDto> {
        val parsedProvider = headers.importProvider()
        val parsedPayloadType = headers.importPayloadType()
        val parsedCityId = headers.importCityId()

        val result = importService.importBatch(
            provider = parsedProvider,
            payloadType = parsedPayloadType,
            cityId = parsedCityId,
            payload = body
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(
            ImportBatchResponseDto(
                importId = result.importId,
                totalProcessed = result.totalProcessed,
                successful = result.successful,
                failed = result.failed,
                results = result.results.map { item ->
                    ImportItemResultDto(
                        index = item.index,
                        outcome = item.outcome,
                        providerExternalId = item.providerExternalId,
                        restroomId = item.restroomId,
                        buildingId = item.buildingId,
                        duplicateOfRestroomId = item.duplicateOfRestroomId,
                        duplicateReason = item.duplicateReason,
                        errorCode = item.errorCode,
                        errorMessage = item.errorMessage
                    )
                }
            )
        )
    }

    private fun HttpHeaders.importProvider(): ImportProvider {
        val raw = firstOrNull("Import-Provider") ?: firstOrNull("X-Import-Provider")
        return parseEnum(raw, "Import-Provider", ImportProvider::valueOf)
    }

    private fun HttpHeaders.importPayloadType(): ImportPayloadType {
        val raw = firstOrNull("Import-Payload-Type") ?: firstOrNull("X-Import-Payload-Type")
        return parseEnum(raw, "Import-Payload-Type", ImportPayloadType::valueOf)
    }

    private fun HttpHeaders.importCityId(): UUID? {
        val raw = firstOrNull("Import-City-Id") ?: firstOrNull("X-Import-City-Id")
        return raw?.let {
            try {
                UUID.fromString(it)
            } catch (_: IllegalArgumentException) {
                throw BadRequestException("Invalid Import-City-Id header")
            }
        }
    }

    private fun HttpHeaders.firstOrNull(name: String): String? = this[name]?.firstOrNull()

    private fun <T> parseEnum(
        raw: String?,
        headerName: String,
        parser: (String) -> T
    ): T {
        if (raw.isNullOrBlank()) {
            throw BadRequestException("$headerName header is required")
        }
        return try {
            parser(raw)
        } catch (_: IllegalArgumentException) {
            throw BadRequestException("Invalid $headerName header")
        }
    }
}
