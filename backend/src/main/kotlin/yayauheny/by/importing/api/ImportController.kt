package yayauheny.by.importing.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import yayauheny.by.common.errors.BadRequestException
import yayauheny.by.importing.service.ImportService
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.ImportBatchResponseDto
import yayauheny.by.model.imports.ImportItemResultDto
import java.util.UUID

@RestController
@RequestMapping("/api/v1/import")
class ImportController(
    private val importService: ImportService
) {
    @PostMapping("/batch")
    fun importBatch(
        @RequestHeader
        headers: HttpHeaders,
        @RequestBody body: JsonNode
    ): ResponseEntity<ImportBatchResponseDto> {
        val result = importService.importBatch(
            provider = headers.importProvider(),
            payloadType = headers.importPayloadType(),
            cityId = headers.importCityId(),
            payload = normalizeBatchBody(body)
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

    private fun normalizeBatchBody(body: JsonNode): JsonNode =
        if (body.isArray) {
            JsonNodeFactory.instance.objectNode().set<JsonNode>("items", body)
        } else {
            body
        }

    private fun HttpHeaders.importProvider(): ImportProvider {
        val raw = firstValue("Import-Provider") ?: firstValue("X-Import-Provider")
        return parseEnum(raw, "Import-Provider", ImportProvider::valueOf)
    }

    private fun HttpHeaders.importPayloadType(): ImportPayloadType {
        val raw = firstValue("Import-Payload-Type") ?: firstValue("X-Import-Payload-Type")
        return parseEnum(raw, "Import-Payload-Type", ImportPayloadType::valueOf)
    }

    private fun HttpHeaders.importCityId(): UUID? {
        val raw = firstValue("Import-City-Id") ?: firstValue("X-Import-City-Id")
        return raw?.let {
            try {
                UUID.fromString(it)
            } catch (_: IllegalArgumentException) {
                throw BadRequestException("Invalid Import-City-Id header")
            }
        }
    }

    private fun HttpHeaders.firstValue(name: String): String? = this[name]?.firstOrNull()

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
