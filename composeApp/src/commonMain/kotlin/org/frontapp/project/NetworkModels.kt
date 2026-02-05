package org.frontapp.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsistenciaDniRequest(val dni: String)

@Serializable
data class AsistenciaRegisterRequest(val dni: String, val fullName: String)

@Serializable
data class RegisterResultResponse(
    val message: String,
    val status: String
)

@Serializable
data class AsistenciaResponse(
    val fullName: String,
    val dni: String,
    val status: String? = null,
    val createdAt: String? = null,
    val position: String? = null,
    val company: String? = null,
    val cargo: String? = null,
    val institucion: String? = null
)

@Serializable
data class UserResponse(
    @SerialName("_id") val id: String,
    val fullName: String,
    val dni: String,
    val position: String? = null,
    val company: String? = null,
    val cargo: String? = null,
    val institucion: String? = null
)

@Serializable
data class UserUpdateRequest(
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dni: String? = null,
    val company: String? = null,
    val position: String? = null
)

@Serializable
data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
