package yayauheny.by.importing.api

import com.fasterxml.jackson.databind.JsonNode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.importing.service.ImportService
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.ImportBatchResponseDto
import yayauheny.by.model.import.ImportItemResultDto
import yayauheny.by.model.import.ImportResponseDto
import java.util.UUID

@RestController
@RequestMapping("/import")
class ImportController(
    private val importService: ImportService
) {
    private val logger = LoggerFactory.getLogger(ImportController::class.java)

    @PostMapping
    suspend fun importSingle(
        @RequestHeader("X-Import-Provider") provider: String,
        @RequestHeader("X-Import-Payload-Type") payloadType: String,
        @RequestHeader("X-Import-City-Id", required = false) cityId: String?,
        @RequestBody body: JsonNode
    ): ResponseEntity<ImportResponseDto> {
        val parsedProvider = ImportProvider.valueOf(provider)
        val parsedPayloadType = ImportPayloadType.valueOf(payloadType)
        val parsedCityId = cityId?.let { UUID.fromString(it) }

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
    suspend fun importBatch(
        @RequestHeader("X-Import-Provider") provider: String,
        @RequestHeader("X-Import-Payload-Type") payloadType: String,
        @RequestHeader("X-Import-City-Id", required = false) cityId: String?,
        @RequestBody body: JsonNode
    ): ResponseEntity<ImportBatchResponseDto> {
        val parsedProvider = ImportProvider.valueOf(provider)
        val parsedPayloadType = ImportPayloadType.valueOf(payloadType)
        val parsedCityId = cityId?.let { UUID.fromString(it) }

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
}
