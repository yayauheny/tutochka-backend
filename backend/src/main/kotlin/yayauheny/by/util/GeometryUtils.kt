package yayauheny.by.util

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import yayauheny.by.model.dto.Coordinates

private val geometryFactory = GeometryFactory()

fun Coordinates.toPoint(): Point = point(lon, lat)

fun Point?.toCoordinates(): Coordinates = Coordinates(lat = this?.y ?: 0.0, lon = this?.x ?: 0.0)

fun point(lon: Double, lat: Double): Point = geometryFactory.createPoint(Coordinate(lon, lat))
