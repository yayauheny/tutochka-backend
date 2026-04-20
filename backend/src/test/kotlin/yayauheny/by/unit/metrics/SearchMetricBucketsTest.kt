package yayauheny.by.unit.metrics

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import yayauheny.by.metrics.SearchMetricBuckets

class SearchMetricBucketsTest {
    @Test
    fun `radius bucket should map correctly`() {
        assertEquals("0_300", SearchMetricBuckets.radiusBucket(100))
        assertEquals("0_300", SearchMetricBuckets.radiusBucket(300))
        assertEquals("301_500", SearchMetricBuckets.radiusBucket(500))
        assertEquals("501_1000", SearchMetricBuckets.radiusBucket(1000))
        assertEquals("1000_plus", SearchMetricBuckets.radiusBucket(1500))
    }

    @Test
    fun `result bucket should map correctly`() {
        assertEquals("0", SearchMetricBuckets.resultBucket(0))
        assertEquals("1_2", SearchMetricBuckets.resultBucket(1))
        assertEquals("1_2", SearchMetricBuckets.resultBucket(2))
        assertEquals("3_5", SearchMetricBuckets.resultBucket(3))
        assertEquals("3_5", SearchMetricBuckets.resultBucket(5))
        assertEquals("6_plus", SearchMetricBuckets.resultBucket(6))
    }

    @Test
    fun `quality bucket should map correctly`() {
        assertEquals("zero", SearchMetricBuckets.qualityBucket(0, null))
        assertEquals("poor", SearchMetricBuckets.qualityBucket(1, 200))
        assertEquals("poor", SearchMetricBuckets.qualityBucket(3, 1500))
        assertEquals("strong", SearchMetricBuckets.qualityBucket(3, 400))
        assertEquals("ok", SearchMetricBuckets.qualityBucket(3, 700))
    }

    @Test
    fun `radius bucket fallback should handle missing and invalid values safely`() {
        assertEquals("0_300", SearchMetricBuckets.radiusBucketOrFallback(null, 300))
        assertEquals("unknown", SearchMetricBuckets.radiusBucketOrFallback("abc"))
        assertEquals("unknown", SearchMetricBuckets.radiusBucketOrFallback("-1"))
        assertEquals("501_1000", SearchMetricBuckets.radiusBucketOrFallback("800"))
    }
}
