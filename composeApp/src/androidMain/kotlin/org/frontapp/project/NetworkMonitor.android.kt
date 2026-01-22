package org.frontapp.project

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

// 1. La Implementación Real (El Marinero Android trabaja)
class AndroidNetworkMonitor(
    private val context: Context
) : NetworkMonitor {

    override val isConnected: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Función suspendida para no bloquear la UI
        suspend fun checkRealInternet(): Boolean {
            return try {
                withContext(Dispatchers.IO) {
                    val url = URL("https://clients3.google.com/generate_204")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 1500
                    connection.readTimeout = 1500
                    connection.connect()
                    connection.responseCode == 204
                }
            } catch (e: Exception) {
                false
            }
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch { trySend(checkRealInternet()) }
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Chequeo inicial
        launch { trySend(checkRealInternet()) }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
}

// 2. Variables para conectar con MainActivity
lateinit var appContext: Context

fun initNetworkMonitor(context: Context) {
    appContext = context.applicationContext
}

// 3. Cumplimos la promesa (actual)
actual fun getNetworkMonitor(): NetworkMonitor {
    return AndroidNetworkMonitor(context = appContext)
}