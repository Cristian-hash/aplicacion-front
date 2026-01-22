package org.frontapp.project

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Monitor "falso" para iOS (siempre dice que sí hay internet por ahora)
class IosNetworkMonitor : NetworkMonitor {
    override val isConnected: Flow<Boolean> = flowOf(true)
}

actual fun getNetworkMonitor(): NetworkMonitor {
    return IosNetworkMonitor()
}