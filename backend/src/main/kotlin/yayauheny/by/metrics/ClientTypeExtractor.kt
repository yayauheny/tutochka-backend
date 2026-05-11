package yayauheny.by.metrics

import io.ktor.http.Headers

fun Headers.extractClientType(): String = MetricLabelWhitelist.clientTypeOrDefault(this["X-Client-Type"])
