package yayauheny.by.helpers

import org.mockito.ArgumentMatchers

@Suppress("UNCHECKED_CAST")
fun <T> any(): T {
    ArgumentMatchers.any<T>()
    return null as T
}

fun <T> eq(value: T): T {
    ArgumentMatchers.eq(value)
    return value
}

fun anyInt(): Int {
    ArgumentMatchers.anyInt()
    return 0
}

fun anyDouble(): Double {
    ArgumentMatchers.anyDouble()
    return 0.0
}
