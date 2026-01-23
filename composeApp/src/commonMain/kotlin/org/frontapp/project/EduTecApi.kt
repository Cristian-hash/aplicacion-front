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

    // --- REGISTRAR POR DNI (El que usamos para QR y Manual) ---
    suspend fun registrarPorDni(dni: String): Result<Boolean> {
        return try {
            val response = client.post("$BASE_URL/register-by-dni") {
                contentType(ContentType.Application.Json)
                setBody(AsistenciaDniRequest(dni))
            }
            
            when (response.status) {
                HttpStatusCode.Created, HttpStatusCode.OK -> Result.success(true)
                HttpStatusCode.NotFound -> {
                    // El usuario no existe en la BD de Mongo
                    Result.failure(Exception("EL USUARIO NO EXISTE"))
                }
                else -> Result.failure(Exception("ERROR DE SERVIDOR"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
