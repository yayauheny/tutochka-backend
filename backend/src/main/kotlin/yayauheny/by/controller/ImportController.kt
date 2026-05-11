package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import java.util.UUID
import org.slf4j.LoggerFactory
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.model.import.ImportBatchResponseDto
import yayauheny.by.model.import.ImportRequestDto
import yayauheny.by.model.import.ImportResponseDto
import yayauheny.by.service.import.ImportService
import yayauheny.by.service.validation.ImportItemsParams
import yayauheny.by.service.validation.validateImportItemsParams
import yayauheny.by.service.validation.validateOrThrow
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
                    val request = parseImportBody(call.receive())
                    ImportItemsParams(request.items, isBatch = false).validateOrThrow(validateImportItemsParams)
                    val cityId = requireCityId(headers)

                    logger.info(
                        "Import request received: provider={}, payloadType={}, cityId={}",
                        headers.provider,
                        headers.payloadType,
                        cityId
                    )

                    val payload = request.items.single()
                    val result =
                        importService.import(
                            provider = headers.provider,
                            payloadType = headers.payloadType,
                            cityId = cityId,
                            payload = payload
                        )

                    val response =
                        ImportResponseDto(
                            importId = result.importId,
                            restroomId = result.restroomId,
                            buildingId = result.buildingId,
                            status = result.status
                        )

                    logger.info(
                        "Import completed successfully: importId={}, restroomId={}, buildingId={}, status={}",
                        result.importId,
                        result.restroomId,
                        result.buildingId,
                        result.status
                    )

                    call.respond(HttpStatusCode.Created, response)
                } catch (e: ValidationException) {
                    logger.warn("Import validation failed: {}", e.message)
                    throw e
                } catch (e: Exception) {
                    logger.error("Import failed", e)
                    throw e
                }
            }

            post("/batch") {
                try {
                    val headers = call.request.headers.getImportHeaders()
                    val request = parseImportBody(call.receive())
                    ImportItemsParams(request.items, isBatch = true).validateOrThrow(validateImportItemsParams)
                    val cityId = requireCityId(headers)

                    logger.info(
                        "Batch import request received: provider={}, payloadType={}, cityId={}",
                        headers.provider,
                        headers.payloadType,
                        cityId
                    )

                    val payload = buildJsonObjectWithItems(request.items)
                    val result =
                        importService.importBatch(
                            provider = headers.provider,
                            payloadType = headers.payloadType,
                            cityId = cityId,
                            payload = payload
                        )

                    val response =
                        ImportBatchResponseDto(
                            importId = result.importId,
                            totalProcessed = result.totalProcessed,
                            successful = result.successful,
                            failed = result.failed,
                        )

                    logger.info(
                        "Batch import completed: importId={}, totalProcessed={}, successful={}, failed={}",
                        result.importId,
                        result.totalProcessed,
                        result.successful,
                        result.failed
                    )

                    call.respond(HttpStatusCode.Created, response)
                } catch (e: ValidationException) {
                    logger.warn("Batch import validation failed: {}", e.message)
                    throw e
                } catch (e: Exception) {
                    logger.error("Batch import failed", e)
                    throw e
                }
            }
        }
    }

    private fun requireCityId(headers: yayauheny.by.util.ImportHeaders): UUID =
        headers.cityId
            ?: throw ValidationException(
                listOf(FieldError("cityId", "cityId header is required for import"))
            )

    private fun parseImportBody(body: JsonObject): ImportRequestDto {
        val itemsElement =
            body["items"]
                ?: throw ValidationException(listOf(FieldError("items", "Items are required")))
        val itemsArray = itemsElement.jsonArray
        val items = itemsArray.mapNotNull { it as? JsonObject }
        if (items.size != itemsArray.size) {
            throw ValidationException(listOf(FieldError("items", "Each item must be a JSON object")))
        }
        return ImportRequestDto(items = items)
    }

    private fun buildJsonObjectWithItems(items: List<JsonObject>): JsonObject = JsonObject(mapOf("items" to JsonArray(items)))
}
