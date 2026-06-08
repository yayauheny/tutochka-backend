package yayauheny.by.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.model.dto.Coordinates

class GeometryUtilsTest {
    @Test
    fun `point helper stores longitude and latitude in the expected order`() {
        val geometry = point(27.56, 53.9)

        assertThat(geometry.x).isEqualTo(27.56)
        assertThat(geometry.y).isEqualTo(53.9)
    }

    @Test
    fun `coordinates round trip through point and back`() {
        val coordinates = Coordinates(lat = 53.9, lon = 27.56)

        val result = coordinates.toPoint().toCoordinates()

        assertThat(result).isEqualTo(coordinates)
    }
}
