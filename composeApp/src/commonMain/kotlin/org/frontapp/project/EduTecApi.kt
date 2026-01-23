package org.frontapp.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object EduTecApi {

    // -------------------------------------------------------------------------
    // ⚠️ CONFIGURACIÓN DE LA IP (¡CAMBIA ESTO!)
    // -------------------------------------------------------------------------
    // OPCIÓN A: Si usas el EMULADOR de Android Studio:
    // private const val BASE_URL = "http://10.0.2.2:8080/api/edutec"

    // Usamos HTTPS porque es un dominio web seguro
    private const val BASE_URL = "https://api.edutec.grupoupgrade.com.pe/api/edutec"
    // -------------------------------------------------------------------------

    // 1. Configuración del Cliente HTTP (El navegador interno de la app)
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Si el back manda campos extra, no explota
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // =========================================================================
    // 2. FUNCIONES DEL SERVIDOR (ENDPOINTS)
    // =========================================================================

    // --- A. REGISTRAR ASISTENCIA (CÁMARA QR) ---
    // POST /register
    suspend fun registrarPorQr(dni: String, fullName: String): Boolean {
        val response = client.post("$BASE_URL/register") {
            contentType(ContentType.Application.Json)
            setBody(AsistenciaRegisterRequest(dni, fullName))
        }
        return response.status == HttpStatusCode.Created // Devuelve true si fue 201
    }

    // --- B. REGISTRAR ASISTENCIA (MANUAL DNI) ---
    // POST /register-by-dni
    suspend fun registrarPorDni(dni: String): Boolean {
        val response = client.post("$BASE_URL/register-by-dni") {
            contentType(ContentType.Application.Json)
            setBody(AsistenciaDniRequest(dni))
        }
        return response.status == HttpStatusCode.Created
    }

    // --- C. OBTENER HISTORIAL (EN VIVO) ---
    // GET /history
    suspend fun obtenerHistorial(): List<AsistenciaResponse> {
        return try {
            val response = client.get("$BASE_URL/history")
            if (response.status == HttpStatusCode.OK) {
                response.body() // Convierte el JSON a List<AsistenciaResponse>
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo historial: ${e.message}")
            emptyList()
        }
    }

    // --- D. DESCARGAR BASE DE DATOS DE USUARIOS (PARA EL BUSCADOR) ---
    // GET /get-all-registrations
    suspend fun obtenerUsuarios(): List<UserResponse> {
        return try {
            val response = client.get("$BASE_URL/get-all-registrations")
            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo usuarios: ${e.message}")
            emptyList()
        }
    }

    // --- E. CORREGIR USUARIO (ZONA 3 - LÁPIZ) ---
    // PUT /edit-register/{id}
    suspend fun corregirUsuario(id: String, dniNuevo: String): Boolean {
        val response = client.put("$BASE_URL/edit-register/$id") {
            contentType(ContentType.Application.Json)
            // Solo enviamos el DNI porque es lo que queremos corregir
            setBody(UserUpdateRequest(dni = dniNuevo))
        }
        return response.status == HttpStatusCode.OK
    }
}