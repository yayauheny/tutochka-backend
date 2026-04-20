package yayauheny.by.metrics

import yayauheny.by.config.ApiConstants

object SearchMetricBuckets {
    const val UNKNOWN_BUCKET = "unknown"

    fun radiusBucket(distanceMeters: Int): String =
        when {
            distanceMeters <= 300 -> "0_300"
            distanceMeters <= 500 -> "301_500"
            distanceMeters <= 1000 -> "501_1000"
            else -> "1000_plus"
        }

    fun radiusBucketOrFallback(
        rawDistanceMeters: String?,
        defaultDistanceMeters: Int = ApiConstants.DEFAULT_MAX_DISTANCE_METERS
    ): String =
        when {
            rawDistanceMeters == null -> radiusBucket(defaultDistanceMeters)
            rawDistanceMeters.toIntOrNull() == null -> UNKNOWN_BUCKET
            rawDistanceMeters.toInt() < 0 -> UNKNOWN_BUCKET
            else -> radiusBucket(rawDistanceMeters.toInt())
        }

    fun resultBucket(resultsCount: Int): String =
        when {
            resultsCount == 0 -> "0"
            resultsCount <= 2 -> "1_2"
            resultsCount <= 5 -> "3_5"
            else -> "6_plus"
        }

    fun qualityBucket(
        resultsCount: Int,
        firstDistanceMeters: Int?
    ): String =
        when {
            resultsCount == 0 -> "zero"
            resultsCount <= 2 -> "poor"
            firstDistanceMeters != null && firstDistanceMeters > 1000 -> "poor"
            resultsCount >= 3 && firstDistanceMeters != null && firstDistanceMeters <= 500 -> "strong"
            else -> "ok"
        }
}
