package yayauheny.by.common.errors

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String,
    val message: String
)
