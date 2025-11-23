package yayauheny.by.util

import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

private const val SRID = 4326

/** POINT(lon, lat) c типом целевого поля */
fun <T> pointExpr(
    lon: Double,
    lat: Double,
    target: Field<T>
): Field<T> = DSL.field("ST_SetSRID(ST_MakePoint({0}, {1}), {2})", target.dataType, lon, lat, SRID)

fun Field<*>.lon(): Field<Double> = DSL.field("ST_X({0})", SQLDataType.DOUBLE, this)

fun Field<*>.lat(): Field<Double> = DSL.field("ST_Y({0})", SQLDataType.DOUBLE, this)

fun Field<*>.latAlias(): Field<Double> = this.lat().`as`("lat")

fun Field<*>.lonAlias(): Field<Double> = this.lon().`as`("lon")

/** Быстрое сферическое расстояние (метры) */
fun Field<*>.distanceSphereTo(
    lat: Double,
    lon: Double
): Field<Double> =
    DSL.field(
        "ST_DistanceSphere({0}, ST_SetSRID(ST_MakePoint({1},{2}), {3}))",
        SQLDataType.DOUBLE,
        this,
        lon,
        lat,
        SRID
    )

/** Точное расстояние по эллипсоиду (через geography) */
fun Field<*>.distanceGeographyTo(
    lat: Double,
    lon: Double
): Field<Double> =
    DSL.field(
        "ST_Distance({0}::geography, ST_SetSRID(ST_MakePoint({1},{2}),{3})::geography)",
        SQLDataType.DOUBLE,
        this,
        lon,
        lat,
        SRID
    )

/** Индексное KNN-упорядочивание (GiST `<->`) */
fun Field<*>.knnOrderTo(
    lat: Double,
    lon: Double
): Field<Double> =
    DSL.field(
        "{0} <-> ST_SetSRID(ST_MakePoint({1},{2}),{3})",
        SQLDataType.DOUBLE,
        this,
        lon,
        lat,
        SRID
    )

/** Индекс-дружественный фильтр по радиусу (geometry) */
fun Field<*>.withinDistanceOf(
    lat: Double,
    lon: Double,
    meters: Double
): Condition =
    DSL.condition(
        "ST_DWithin({0}, ST_SetSRID(ST_MakePoint({1},{2}),{3}), {4})",
        this,
        lon,
        lat,
        SRID,
        meters
    )

/** GeoJSON -> geometry, тип целевого поля берём из target */
fun <T> geomFromGeoJson(
    geoJson: String,
    target: Field<T>
): Field<T> = DSL.field("ST_SetSRID(ST_GeomFromGeoJSON({0}), {1})", target.dataType, geoJson, SRID)

/** geometry -> GeoJSON */
fun Field<*>.asGeoJson(): Field<String> = DSL.field("ST_AsGeoJSON({0})", SQLDataType.VARCHAR, this)

/** безопасно достаём double из Record — упадём с понятной ошибкой, если забыли включить колонку в SELECT */
fun Record.reqDouble(name: String): Double = get(name, Double::class.java) ?: error("Expected column '$name' in SELECT")
