package org.frontapp.project

import kotlinx.coroutines.flow.Flow

// 1. Definimos la Interfaz (El contrato)
interface NetworkMonitor {
    val isConnected: Flow<Boolean>
}

// 2. La Promesa (El Capitán dice: "Necesito un monitor")
expect fun getNetworkMonitor(): NetworkMonitor