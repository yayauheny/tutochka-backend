package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.model.import.ImportBatchResponseDto
import yayauheny.by.model.import.ImportItemResultDto
import yayauheny.by.model.import.ImportPayloadType
import yayauheny.by.model.import.ImportRequestDto
import yayauheny.by.model.import.ImportResponseDto
import yayauheny.by.service.import.ImportService

class ImportController(
    private val importService: ImportService
) {
    private val logger = LoggerFactory.getLogger(ImportController::class.java)

    fun Route.importRoutes() {
        route("/import") {
            post {
                try {
                    val request = call.receive<ImportRequestDto>()

                    validatePayload(request)

                    logger.info(
                        "Import request received: provider={}, payloadType={}, cityId={}",
                        request.provider,
                        request.payloadType,
                        request.cityId
                    )

                    val result =
                        importService.import(
                            provider = request.provider,
                            payloadType = request.payloadType,
                            cityId = request.cityId,
                            payload = request.payload
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
                    val request = call.receive<ImportRequestDto>()

                    validatePayload(request)

                    logger.info(
                        "Batch import request received: provider={}, payloadType={}, cityId={}",
                        request.provider,
                        request.payloadType,
                        request.cityId
                    )

                    val result =
                        importService.importBatch(
                            provider = request.provider,
                            payloadType = request.payloadType,
                            cityId = request.cityId,
                            payload = request.payload
                        )

                    val response =
                        ImportBatchResponseDto(
                            importId = result.importId,
                            totalProcessed = result.totalProcessed,
                            successful = result.successful,
                            failed = result.failed,
                            results =
                                result.results.map { itemResult ->
                                    ImportItemResultDto(
                                        index = itemResult.index,
                                        restroomId = itemResult.restroomId,
                                        buildingId = itemResult.buildingId,
                                        success = itemResult.success,
                                        errorMessage = itemResult.errorMessage
                                    )
                                }
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

    private fun validatePayload(request: ImportRequestDto) {
        if (request.payload.isEmpty()) {
            throw ValidationException(listOf(FieldError("payload", "Payload cannot be empty")))
        }

        when (request.payloadType) {
            ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON -> {
                validateScrapedPayload(request.payload)
            }
            else -> {
                throw IllegalArgumentException("Unacceptable payload type: ${request.payloadType}")
            }
        }
    }

    private fun validateScrapedPayload(payload: JsonObject) {
        // Проверяем формат scraped: либо {"items": [...]}, либо одиночный объект с обязательными полями
        val hasItemsArray = payload["items"]?.jsonArray?.isNotEmpty() == true

        if (hasItemsArray) {
            // Проверяем, что элементы массива валидны
            val items = payload["items"]?.jsonArray ?: return
            items.forEachIndexed { index, element ->
                if (element !is JsonObject) {
                    throw ValidationException(
                        listOf(FieldError("payload.items[$index]", "Item must be a JSON object"))
                    )
                }
                validateScrapedItem(element, "items[$index]")
            }
        } else {
            // Проверяем одиночный объект
            validateScrapedItem(payload, "payload")
        }
    }

    private fun validateScrapedItem(
        item: JsonObject,
        fieldPath: String
    ) {
        val requiredFields = listOf("id", "title", "location")
        val missingFields =
            requiredFields.filter { fieldName ->
                val value = item[fieldName]
                value == null || value is JsonNull
            }

        if (missingFields.isNotEmpty()) {
            throw ValidationException(
                listOf(
                    FieldError(
                        fieldPath,
                        "Missing required fields: ${missingFields.joinToString(", ")}"
                    )
                )
            )
        }

        // Проверяем структуру location
        val location = item["location"]?.jsonObject
        if (location != null) {
            val locationFields = listOf("lat", "lng")
            val missingLocationFields =
                locationFields.filter { fieldName ->
                    val value = location[fieldName]
                    value == null || value is JsonNull
                }
            if (missingLocationFields.isNotEmpty()) {
                throw ValidationException(
                    listOf(
                        FieldError(
                            "$fieldPath.location",
                            "Missing required fields: ${missingLocationFields.joinToString(", ")}"
                        )
                    )
                )
            }
        }
    }
}
