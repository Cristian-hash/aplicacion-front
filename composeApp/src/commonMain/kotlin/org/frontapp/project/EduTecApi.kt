package org.frontapp.project

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    // --- REGISTRAR POR DNI (Manual / Ingreso Directo) ---
    suspend fun registrarPorDni(dni: String): Result<RegisterResultResponse> {
        return try {
            val response = client.post("$BASE_URL/register-by-dni") {
                contentType(ContentType.Application.Json)
                setBody(AsistenciaDniRequest(dni))
            }
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REGISTRAR CON NOMBRE (QR) ---
    suspend fun registrarConNombre(dni: String, fullName: String): Result<RegisterResultResponse> {
        return try {
            val response = client.post("$BASE_URL/register") {
                contentType(ContentType.Application.Json)
                setBody(AsistenciaRegisterRequest(dni, fullName))
            }
            handleResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Función auxiliar para procesar la respuesta y capturar mensajes del backend
    private suspend fun handleResponse(response: HttpResponse): Result<RegisterResultResponse> {
        return if (response.status.value in 200..299) {
            Result.success(response.body())
        } else {
            try {
                // Intentamos leer el JSON de error del backend (ApiErrorResponse)
                val error: ApiErrorResponse = response.body()
                Result.failure(Exception(error.message))
            } catch (e: Exception) {
                Result.failure(Exception("Error en el servidor (${response.status.value})"))
            }
        }
    }

    suspend fun obtenerHistorial(): List<AsistenciaResponse> {
        return try {
            val response = client.get("$BASE_URL/history")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerUsuarios(): List<UserResponse> {
        return try {
            val response = client.get("$BASE_URL/get-all-registrations")
            if (response.status == HttpStatusCode.OK) response.body() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun corregirUsuario(id: String, dniNuevo: String): Boolean {
        return try {
            val response = client.put("$BASE_URL/edit-register/$id") {
                contentType(ContentType.Application.Json)
                setBody(UserUpdateRequest(dni = dniNuevo))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }
}
