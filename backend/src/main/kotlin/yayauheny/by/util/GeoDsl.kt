package yayauheny.by.util

import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

private const val SRID = 4326

/** Создает POINT(lon, lat) с SRID 4326 */
fun <T> pointExpr(
    lon: Double,
    lat: Double,
    target: Field<T>
): Field<T> = DSL.field("ST_SetSRID(ST_MakePoint({0}, {1}), {2})", target.dataType, lon, lat, SRID)

fun Field<*>.lon(): Field<Double> = DSL.field("ST_X(({0})::geometry)", SQLDataType.DOUBLE, this)

fun Field<*>.lat(): Field<Double> = DSL.field("ST_Y(({0})::geometry)", SQLDataType.DOUBLE, this)

fun Field<*>.latAlias(): Field<Double> = this.lat().`as`("lat")

fun Field<*>.lonAlias(): Field<Double> = this.lon().`as`("lon")

/** Быстрое сферическое расстояние в метрах */
fun Field<*>.distanceSphereTo(
    lat: Double,
    lon: Double
): Field<Double> =
    DSL.field(
        "ST_DistanceSphere(({0})::geometry, ST_SetSRID(ST_MakePoint({1},{2}), {3}))",
        SQLDataType.DOUBLE,
        this,
        lon,
        lat,
        SRID
    )

/** Точное расстояние по эллипсоиду в метрах (geography) */
fun Field<*>.distanceGeographyTo(
    lat: Double,
    lon: Double
): Field<Double> =
    DSL.field(
        "ST_Distance(({0})::geometry::geography, ST_SetSRID(ST_MakePoint({1},{2}),{3})::geography)",
        SQLDataType.DOUBLE,
        this,
        lon,
        lat,
        SRID
    )

/** KNN-сортировка через GiST индекс (`<->`) */
fun Field<*>.knnOrderTo(
    lat: Double,
    lon: Double
): Field<Double> =
    DSL.field(
        "({0})::geometry <-> ST_SetSRID(ST_MakePoint({1},{2}),{3})",
        SQLDataType.DOUBLE,
        this,
        lon,
        lat,
        SRID
    )

/** Фильтр по радиусу с использованием индекса */
fun Field<*>.withinDistanceOf(
    lat: Double,
    lon: Double,
    meters: Double
): Condition =
    DSL.condition(
        "ST_DWithin(({0})::geometry, ST_SetSRID(ST_MakePoint({1},{2}),{3}), {4})",
        this,
        lon,
        lat,
        SRID,
        meters
    )

fun Field<*>.withinGeographyDistanceOf(
    lat: Double,
    lon: Double,
    meters: Double
): Condition =
    DSL.condition(
        "ST_DWithin(({0})::geometry::geography, ST_SetSRID(ST_MakePoint({1},{2}),{3})::geography, {4})",
        this,
        lon,
        lat,
        SRID,
        meters
    )

/** Преобразует GeoJSON строку в geometry с SRID 4326 */
fun <T> geomFromGeoJson(
    geoJson: String,
    target: Field<T>
): Field<T> = DSL.field("ST_SetSRID(ST_GeomFromGeoJSON({0}), {1})", target.dataType, geoJson, SRID)

/** Преобразует geometry в GeoJSON строку */
fun Field<*>.asGeoJson(): Field<String> = DSL.field("ST_AsGeoJSON(({0})::geometry)", SQLDataType.VARCHAR, this)

/** Извлекает double из Record, выбрасывает ошибку если колонка отсутствует */
fun Record.reqDouble(name: String): Double = get(name, Double::class.java) ?: error("Ожидалась колонка '$name' в SELECT")
