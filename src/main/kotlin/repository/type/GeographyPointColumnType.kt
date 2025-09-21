package yayauheny.by.repository.type

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.postgresql.util.PGobject
import yayauheny.by.model.GeoPoint

class GeographyPointColumnType : ColumnType<GeoPoint>() {
    companion object {
        val regex = Regex("""POINT\(([\d.]+) ([\d.]+)\)""")
    }

    override fun sqlType(): String = "GEOGRAPHY(Point, 4326)"

    override fun valueFromDB(value: Any): GeoPoint? {
        return when (value) {
            is String -> {
                regex.find(value)?.destructured?.let { (lon, lat) ->
                    GeoPoint(lon.toDouble(), lat.toDouble())
                }
            }
            is PGobject -> {
                value.value?.let { stringValue ->
                    regex.find(stringValue)?.destructured?.let { (lon, lat) ->
                        GeoPoint(lon.toDouble(), lat.toDouble())
                    }
                }
            }
            else -> {
                val stringValue = value.toString()
                regex.find(stringValue)?.destructured?.let { (lon, lat) ->
                    GeoPoint(lon.toDouble(), lat.toDouble())
                }
            }
        }
    }

    override fun valueToDB(value: GeoPoint?): Any {
        requireNotNull(value)
        return when (currentDialect) {
            is PostgreSQLDialect ->
                PGobject().apply {
                    type = "geography"
                    this.value = "SRID=4326;POINT(${value.longitude} ${value.latitude})"
                }
            else -> "POINT(${value.longitude} ${value.latitude})"
        }
    }

    override fun notNullValueToDB(value: GeoPoint): Any =
        when (currentDialect) {
            is PostgreSQLDialect ->
                PGobject().apply {
                    type = "geography"
                    this.value = "SRID=4326;POINT(${value.longitude} ${value.latitude})"
                }
            else -> "POINT(${value.longitude} ${value.latitude})"
        }

    override fun setParameter(
        stmt: PreparedStatementApi,
        index: Int,
        value: Any?
    ) {
        if (value is GeoPoint) {
            val param = valueToDB(value)
            stmt[index] = param
        } else {
            super.setParameter(stmt, index, value)
        }
    }
}
