package yayauheny.by.metrics

import io.ktor.server.application.ApplicationCall

fun ApplicationCall.extractClientType(): String = MetricLabelWhitelist.clientTypeOrDefault(request.headers["X-Client-Type"])
