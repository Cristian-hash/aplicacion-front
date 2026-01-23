package org.frontapp.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==========================================
// 1. DTOs PARA EL REGISTRO (AsistenciaController)
// ==========================================

// Backend: AsistenciaDniDto
// Uso: Para el "Ingreso Directo" (Manual) -> POST /register-by-dni
@Serializable
data class AsistenciaDniRequest(
    val dni: String
)

// Backend: AsistenciaRegisterDto
// Uso: Para el "Escáner QR" -> POST /register
@Serializable
data class AsistenciaRegisterRequest(
    val dni: String,
    val fullName: String
)

// Nuevo DTO para capturar la respuesta del registro
@Serializable
data class RegisterResultResponse(
    val message: String,
    val status: String
)

// Backend: AsistenciaViewDto
// Uso: Lo que recibimos en el Historial -> GET /history
@Serializable
data class AsistenciaResponse(
    val fullName: String,
    val dni: String,
    val status: String? = "INGRESO" // Agregamos status para el historial
)

// ==========================================
// 2. DTOs PARA GESTIÓN DE USUARIOS (RegistrationController)
// ==========================================

// Backend: RegistrationViewDto
// Uso: Para llenar la lista del Buscador -> GET /get-all-registrations
@Serializable
data class UserResponse(
    // IMPORTANTE: Tu backend envía "_id" (con guion bajo por Mongo).
    // Aquí le decimos: "Cuando leas '_id', guárdalo en la variable 'id'".
    @SerialName("_id") val id: String,
    val fullName: String,
    val dni: String
)

// Backend: RegistrationUpdateDto
// Uso: Para corregir datos (Zona 3) -> PUT /edit-register/{id}
@Serializable
data class UserUpdateRequest(
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dni: String? = null,
    val company: String? = null,
    val position: String? = null
)

// ==========================================
// 3. MANEJO DE ERRORES (GlobalExceptionHandler)
// ==========================================

// Backend: ErrorResponse
// Uso: Para leer los mensajes de error del servidor (ej: "El usuario no existe")
@Serializable
data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String, // Este es el texto que mostraremos en la alerta ROJA 🔴
    val path: String
)
