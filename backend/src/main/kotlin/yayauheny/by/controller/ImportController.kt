package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException
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

                    // Выполняем импорт через ImportService
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
                    // ImportService уже сохранил ошибку в restroom_imports через markFailed
                    throw e
                }
            }
        }
    }

    private fun validatePayload(request: ImportRequestDto) {
        if (request.payload.isEmpty()) {
            throw ValidationException(listOf(FieldError("payload", "Payload cannot be empty")))
        }

        val hasId = request.payload["id"] != null
        val hasResultItems =
            request.payload["result"]
                ?.jsonObject
                ?.get("items")
                ?.jsonArray
                ?.isNotEmpty() == true

        if (!hasId && !hasResultItems) {
            throw ValidationException(
                listOf(
                    FieldError(
                        "payload",
                        "Payload must contain either id or result.items with at least one element"
                    )
                )
            )
        }
    }
}
