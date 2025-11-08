package yayauheny.by.util

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Point
import org.jooq.Field
import org.jooq.impl.DSL

private val geoFactory by lazy { GeometryFactory() }

fun createPoint(
    latitude: Double,
    longitude: Double
): Point {
    require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90, got: $latitude" }
    require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180, got: $longitude" }
    return geoFactory.createPoint(Coordinate(longitude, latitude))
}

fun Pair<Double, Double>.toPoint(): Point = createPoint(latitude = first, longitude = second)

fun geometryPoint(
    lat: Double,
    lon: Double
): Field<Geometry> =
    DSL.field(
        "ST_SetSRID(ST_MakePoint({0}, {1}), 4326)",
        Geometry::class.java,
        lon,
        lat
    )

fun stLatitude(geometryField: Field<*>): Field<Double> =
    DSL.field(
        "ST_Y({0})",
        Double::class.java,
        geometryField
    )

fun stLongitude(geometryField: Field<*>): Field<Double> =
    DSL.field(
        "ST_X({0})",
        Double::class.java,
        geometryField
    )

fun stDistance(
    geometryField: Field<*>,
    toLatitude: Double,
    toLongitude: Double
): Field<Double> =
    DSL.field(
        "ST_DistanceSphere({0}, ST_SetSRID(ST_MakePoint({1}, {2}), 4326))",
        Double::class.java,
        geometryField,
        toLongitude,
        toLatitude
    )

fun stDistanceSpheroid(
    geometryField: Field<*>,
    toLatitude: Double,
    toLongitude: Double
): Field<Double> =
    DSL.field(
        "ST_DistanceSpheroid({0}, ST_SetSRID(ST_MakePoint({1}, {2}), 4326), 'SPHEROID[\"WGS 84\",6378137,298.257223563]')",
        Double::class.java,
        geometryField,
        toLongitude,
        toLatitude
    )


fun stDWithin(
    geometryField: Field<*>,
    latitude: Double,
    longitude: Double,
    distanceMeters: Double
): org.jooq.Condition {
    val metersPerDegree = 111_320.0
    val deltaDeg = distanceMeters / metersPerDegree

    val targetPoint: Field<Geometry> =
        DSL.field(
            "ST_SetSRID(ST_MakePoint({0}, {1}), 4326)",
            Geometry::class.java,
            longitude,
            latitude
        )

    val bboxCondition =
        DSL.condition(
            "{0} && ST_Expand({1}, {2})",
            geometryField,
            targetPoint,
            deltaDeg
        )

    val preciseCondition =
        DSL.condition(
            "ST_DistanceSphere({0}, {1}) <= {2}",
            geometryField,
            targetPoint,
            distanceMeters
        )

    return bboxCondition.and(preciseCondition)
}

fun stDistanceOrder(
    geometryField: Field<*>,
    toLatitude: Double,
    toLongitude: Double
): Field<Double> = stDistance(geometryField, toLatitude, toLongitude)
