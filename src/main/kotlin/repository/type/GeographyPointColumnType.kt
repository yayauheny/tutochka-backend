package yayauheny.by.repository.type

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import yayauheny.by.model.GeoPoint

class GeographyPointColumnType : ColumnType<GeoPoint>() {
    companion object {
        val regex = Regex("""POINT\(([\d.]+) ([\d.]+)\)""")
    }

    override fun sqlType(): String = "GEOGRAPHY(Point, 4326)"

    override fun valueFromDB(value: Any): GeoPoint? {
        val stringValue = value.toString()
        return regex.find(stringValue)?.destructured?.let { (lon, lat) ->
            GeoPoint(lon.toDouble(), lat.toDouble())
        }
    }

    override fun valueToDB(value: GeoPoint?): Any {
        requireNotNull(value)
        return when (currentDialect) {
            is PostgreSQLDialect -> "SRID=4326;POINT(${value.longitude} ${value.latitude})"
            else -> "POINT(${value.longitude} ${value.latitude})"
        }
    }

    override fun notNullValueToDB(value: GeoPoint): Any = "POINT(${value.longitude} ${value.latitude})"

    override fun setParameter(
        stmt: PreparedStatementApi,
        index: Int,
        value: Any?
    ) {
        if (value is GeoPoint) {
            stmt[index] = valueToDB(value) as String
        } else {
            super.setParameter(stmt, index, value)
        }
    }
}
