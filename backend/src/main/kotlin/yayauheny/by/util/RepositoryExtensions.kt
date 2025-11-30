package yayauheny.by.util

import org.jooq.Condition
import org.jooq.TableField

fun <R : org.jooq.Record> TableField<R, Boolean?>.isNotDeleted(): Condition = this.eq(false).or(this.isNull)
