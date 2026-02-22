package yayauheny.by.util

import org.jooq.Condition
import org.jooq.Field
import org.jooq.TableField
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.dto.Coordinates

fun <R : org.jooq.Record> TableField<R, Boolean?>.isNotDeleted(): Condition = this.eq(false).or(this.isNull)

fun <T> UpdateSetMoreStep<*>.setIfNotNull(
    field: Field<T>,
    value: T?
) = value?.let { set(field, it) } ?: this

fun <T> UpdateSetMoreStep<*>.setIfNotNullCoordinates(
    coords: Coordinates?,
    coordinatesField: Field<T>
) = coords?.let { set(coordinatesField, pointExpr(it.lon, it.lat, coordinatesField)) } ?: this
