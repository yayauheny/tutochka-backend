package yayauheny.by.common.util

import java.math.RoundingMode

fun Double.toBigDecimalRounded(scale: Int = 2) = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP)
