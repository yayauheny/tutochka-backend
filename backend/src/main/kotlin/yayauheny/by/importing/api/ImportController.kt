package yayauheny.by.importing.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import org.slf4j.LoggerFactory
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.importing.service.ImportService
import yayauheny.by.model.import.ImportBatchResponseDto
import yayauheny.by.model.import.ImportItemResultDto
import yayauheny.by.model.import.ImportRequestDto
import yayauheny.by.model.import.ImportResponseDto
import yayauheny.by.service.validation.ImportItemsParams
import yayauheny.by.service.validation.validateImportItemsParams
import yayauheny.by.service.validation.validateOrThrow
import yayauheny.by.service.validation.validateSingleImportItemsParams
import yayauheny.by.util.getImportHeaders

class ImportController(
    private val importService: ImportService
) {
    private val logger = LoggerFactory.getLogger(ImportController::class.java)

    fun Route.importRoutes() {
        route("/import") {
            post {
                try {
                    val headers = call.request.headers.getImportHeaders()
                    val request = parseImportObjectBody(call.receive())
                    ImportItemsParams(request.items).validateOrThrow(validateSingleImportItemsParams)
                    val payload = request.items.single()
                    val result =
                        importService.import(
                            provider = headers.provider,
                            payloadType = headers.payloadType,
                            cityId = headers.cityId,
                            payload = payload
                        )
                    call.respond(
                        HttpStatusCode.Created,
                        ImportResponseDto(
                            importId = result.importId,
                            restroomId = result.restroomId,
                            buildingId = result.buildingId,
                            status = result.status
                        )
                    )
                } catch (error: ValidationException) {
                    logger.warn("Import validation failed: {}", error.message)
                    throw error
                } catch (error: Exception) {
                    logger.error("Import failed", error)
                    throw error
                }
            }

            post("/batch") {
                try {
                    val headers = call.request.headers.getImportHeaders()
                    val request = parseBatchImportBody(call.receive())
                    ImportItemsParams(request.items).validateOrThrow(validateImportItemsParams)
                    val payload = buildJsonObjectWithItems(request.items)
                    val result =
                        importService.importBatch(
                            provider = headers.provider,
                            payloadType = headers.payloadType,
                            cityId = headers.cityId,
                            payload = payload
                        )

                    call.respond(
                        HttpStatusCode.Created,
                        ImportBatchResponseDto(
                            importId = result.importId,
                            totalProcessed = result.totalProcessed,
                            successful = result.successful,
                            failed = result.failed,
                            results =
                                result.results.map { item ->
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
                } catch (error: ValidationException) {
                    logger.warn("Batch import validation failed: {}", error.message)
                    throw error
                } catch (error: Exception) {
                    logger.error("Batch import failed", error)
                    throw error
                }
            }
        }
    }

    private fun parseImportObjectBody(body: JsonObject): ImportRequestDto {
        val itemsElement =
            body["items"] ?: throw ValidationException(FieldError("items", "Items are required"))
        val itemsArray = itemsElement.jsonArray
        return buildImportRequest(itemsArray)
    }

    private fun parseBatchImportBody(body: JsonElement): ImportRequestDto {
        return when (body) {
            is JsonObject -> parseImportObjectBody(body)
            is JsonArray -> buildImportRequest(body)
            else -> throw ValidationException(FieldError("items", "Items must be a JSON array or an object with items"))
        }
    }

    private fun buildImportRequest(itemsArray: JsonArray): ImportRequestDto {
        val jsons = itemsArray.map { it as? JsonObject }.validateOrThrow()
        return ImportRequestDto(items = jsons)
    }

    private fun buildJsonObjectWithItems(items: List<JsonObject>): JsonObject = JsonObject(mapOf("items" to JsonArray(items)))
}
