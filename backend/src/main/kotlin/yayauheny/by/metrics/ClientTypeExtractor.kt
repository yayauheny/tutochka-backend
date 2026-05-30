package yayauheny.by.metrics

import org.springframework.http.HttpHeaders

fun HttpHeaders.extractClientType(): String = MetricLabelWhitelist.clientTypeOrDefault(this.getFirst("X-Client-Type"))
