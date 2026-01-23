package org.frontapp.project

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==========================================
// 1. PARA EL INGRESO (AsistenciaController)
// ==========================================

// Para el "Ingreso Directo" (Manual) -> POST /register-by-dni
@Serializable
data class AsistenciaDniRequest(
    val dni: String
)

// Para el "Escáner QR" -> POST /register
@Serializable
data class AsistenciaRegisterRequest(
    val dni: String,
    val fullName: String
)

// Lo que recibimos en el Historial -> GET /history
@Serializable
data class AsistenciaResponse(
    val fullName: String,
    val dni: String
    // Nota: Si tu backend agrega 'createdAt' en el futuro, lo pondremos aquí.
)

// ==========================================
// 2. PARA EL BUSCADOR (RegistrationController)
// ==========================================

// Para la lista de búsqueda -> GET /get-all-registrations
@Serializable
data class UserResponse(
    @SerialName("_id") val id: String, // Mapeamos '_id' de Mongo a 'id'
    val fullName: String,
    val dni: String
)

// Para corregir datos (Zona 3) -> PUT /edit-register/{id}
@Serializable
data class UserUpdateRequest(
    val dni: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val company: String? = null,
    val position: String? = null
)

// ==========================================
// 3. MANEJO DE ERRORES
// ==========================================

// Para leer los mensajes de error de tu GlobalExceptionHandler
@Serializable
data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String, // Este es el texto que mostraremos en la alerta roja 🔴
    val path: String
)