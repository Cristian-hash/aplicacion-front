package org.frontapp.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object EduTecApi {

    private const val BASE_URL = "https://api.edutec.grupoupgrade.com.pe/api/edutec"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // --- A. REGISTRAR ASISTENCIA (QR) ---
    // Se usa el endpoint /register que pide DNI y Nombre Completo
    suspend fun registrarPorQr(dni: String, fullName: String): Boolean {
        return try {
            val response = client.post("$BASE_URL/register") {
                contentType(ContentType.Application.Json)
                setBody(AsistenciaRegisterRequest(dni, fullName))
            }
            // Aceptamos 201 Created o 200 OK
            response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("❌ Error en registrarPorQr: ${e.message}")
            false
        }
    }

    // --- B. REGISTRAR ASISTENCIA (DNI MANUAL) ---
    // Se usa el endpoint /register-by-dni que solo pide el DNI
    suspend fun registrarPorDni(dni: String): Boolean {
        return try {
            val response = client.post("$BASE_URL/register-by-dni") {
                contentType(ContentType.Application.Json)
                setBody(AsistenciaDniRequest(dni))
            }
            // Aceptamos 201 Created o 200 OK
            response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("❌ Error en registrarPorDni: ${e.message}")
            false
        }
    }

    // --- C. OBTENER HISTORIAL (EN VIVO) ---
    suspend fun obtenerHistorial(): List<AsistenciaResponse> {
        return try {
            val response = client.get("$BASE_URL/history")
            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo historial: ${e.message}")
            emptyList()
        }
    }

    // --- D. DESCARGAR BASE DE DATOS DE USUARIOS ---
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

    // --- E. CORREGIR USUARIO ---
    suspend fun corregirUsuario(id: String, dniNuevo: String): Boolean {
        return try {
            val response = client.put("$BASE_URL/edit-register/$id") {
                contentType(ContentType.Application.Json)
                setBody(UserUpdateRequest(dni = dniNuevo))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            println("❌ Error corrigiendo usuario: ${e.message}")
            false
        }
    }
}
