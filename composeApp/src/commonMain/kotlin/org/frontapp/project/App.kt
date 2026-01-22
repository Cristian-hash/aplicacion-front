package org.frontapp.project

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ==================================================================
// 1.0 SISTEMA DE DISEÑO
// ==================================================================
object EduTheme {
    val BrandRed = Color(0xFFCF0A2C)
    val BrandBlack = Color(0xFF232323)
    val DarkHeader = Color(0xFF1a1a1a)
    val White = Color(0xFFFFFFFF)
    val GrayBg = Color(0xFFF2F2F2)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFC107)
    val WarningText = Color(0xFF856404)
    val WarningBg = Color(0xFFFFF3CD)
}

// ==================================================================
// 2.0 MODELOS DE DATOS
// ==================================================================
data class User(val id: Long, val name: String, val dni: String, val email: String, val school: String, val phone: String)
data class ScanRecord(val id: Long, val name: String, val dni: String, val time: String, val type: String)

val INITIAL_DB = listOf(
    User(1, "Juan Perez", "12345678", "juan@test.com", "Universidad Nacional", "999888777"),
    User(2, "Maria Gomez", "87654321", "maria@test.com", "Instituto Tecnológico", "999111222")
)

// ==================================================================
// 3.0 LÓGICA DE COMUNICACIÓN
// ==================================================================
suspend fun enviarAInternetReal(record: ScanRecord): Boolean {
    return try {
        println("📡 ENVIANDO: ${record.name}")
        delay(1500) // Simula el viaje por la red
        true
    } catch (e: Exception) {
        false
    }
}

// ==================================================================
// 4.0 PANTALLAS
// ==================================================================

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(EduTheme.White)) {
        Box(modifier = Modifier.offset(y = (-50).dp).size(300.dp).background(EduTheme.BrandRed.copy(0.1f), CircleShape).blur(50.dp).align(Alignment.TopCenter))
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Rounded.QrCode2, null, modifier = Modifier.size(80.dp), tint = EduTheme.BrandRed)
            Text("EduTec", fontSize = 48.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedButton(onClick = { onNavigate("admin") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(20.dp)) {
                Text("Ingreso Staff", fontWeight = FontWeight.Bold, color = EduTheme.BrandBlack)
            }
        }
    }
}

@Composable
fun AdminScreen(onBack: () -> Unit, db: MutableList<User>, scans: MutableList<ScanRecord>, onAddScan: (ScanRecord) -> Unit) {
    val scope = rememberCoroutineScope()
    val networkMonitor = remember { getNetworkMonitor() }
    val isOnline by networkMonitor.isConnected.collectAsState(initial = false)
    val pendingQueue = remember { mutableStateListOf<ScanRecord>() }

    var mode by remember { mutableStateOf("camera") }
    var manualDni by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(isOnline) {
        if (isOnline && pendingQueue.isNotEmpty()) {
            val itemsToSync = pendingQueue.toList()
            itemsToSync.forEach { enviarAInternetReal(it) }
            pendingQueue.clear()
        }
    }

    fun processEntry(user: User, method: String) {
        val isReentry = scans.any { it.dni == user.dni }
        val type = if (isReentry) "Re-ingreso" else method
        val newRecord = ScanRecord(Random.nextLong(), user.name, user.dni, "11:30 AM", type)

        onAddScan(newRecord)

        scope.launch {
            if (isOnline) {
                if (!enviarAInternetReal(newRecord)) pendingQueue.add(newRecord)
            } else {
                pendingQueue.add(newRecord)
            }
        }
        feedback = if (isReentry) "reentry" to "¡YA INGRESÓ!" else "success" to "¡BIENVENIDO!"
    }

    LaunchedEffect(feedback) { if (feedback != null) { delay(2000); feedback = null } }

    Box(modifier = Modifier.fillMaxSize().background(EduTheme.DarkHeader)) {
        Column(modifier = Modifier.zIndex(100f).fillMaxWidth()) {
            if (!isOnline) {
                Box(modifier = Modifier.fillMaxWidth().background(EduTheme.BrandRed).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("⚠️ MODO OFFLINE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (pendingQueue.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFF9800)).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("Sincronizando ${pendingQueue.size}...", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.Black).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("STAFF EDUTEC", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Color.White) }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (mode == "camera") {
                    CameraPreview(
                        reductionFactor = 1f,
                        onCameraStatusChanged = { _, _ -> },
                        onQrDetected = { qr ->
                            val user = db.find { it.dni == qr } ?: User(0, "Desconocido", qr, "", "", "")
                            processEntry(user, "QR")
                        }
                    )
                    Box(modifier = Modifier.size(260.dp).border(2.dp, Color.White.copy(0.4f), RoundedCornerShape(24.dp)).align(Alignment.Center))
                } else {
                    Column(modifier = Modifier.padding(24.dp)) {
                        OutlinedTextField(value = manualDni, onValueChange = { manualDni = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("DNI...") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        Button(onClick = { if (manualDni.isNotEmpty()) { processEntry(db.find { it.dni == manualDni } ?: User(0, "Invitado", manualDni, "", "", ""), "Manual"); manualDni = "" } }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("REGISTRAR") }
                    }
                }
            }

            Row(modifier = Modifier.background(Color.Black).padding(8.dp)) {
                Button(onClick = { mode = "camera" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="camera") Color.DarkGray else Color.Transparent)) { Icon(Icons.Default.CameraAlt, null) }
                Button(onClick = { mode = "manual" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="manual") Color.DarkGray else Color.Transparent)) { Icon(Icons.Default.Edit, null) }
            }

            LazyColumn(modifier = Modifier.height(150.dp).fillMaxWidth().background(Color.White)) {
                items(scans) { scan ->
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(scan.name, fontWeight = FontWeight.Bold); Text(scan.dni, fontSize = 12.sp, color = Color.Gray) }
                        Text(scan.type, color = if(scan.type=="Re-ingreso") EduTheme.WarningText else EduTheme.Success)
                    }
                    Divider()
                }
            }
        }

        AnimatedVisibility(visible = feedback != null, modifier = Modifier.align(Alignment.Center)) {
            feedback?.let { (type, msg) ->
                Card(colors = CardDefaults.cardColors(containerColor = if(type=="success") EduTheme.Success else EduTheme.Warning)) {
                    Text(msg, modifier = Modifier.padding(24.dp), color = Color.White, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("home") }
    val db = remember { mutableStateListOf<User>().apply { addAll(INITIAL_DB) } }
    val scans = remember { mutableStateListOf<ScanRecord>() }

    MaterialTheme {
        Surface {
            when (currentScreen) {
                "home" -> HomeScreen { currentScreen = it }
                "admin" -> AdminScreen(onBack = { currentScreen = "home" }, db = db, scans = scans, onAddScan = { scans.add(0, it) })
            }
        }
    }
}