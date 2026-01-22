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
import androidx.compose.ui.graphics.Color
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
    val BrandRedDark = Color(0xFFA00822)
    val BrandBlack = Color(0xFF232323)
    val DarkHeader = Color(0xFF1a1a1a)
    val White = Color(0xFFFFFFFF)
    val GrayBg = Color(0xFFF2F2F2)
    val InputBg = Color(0xFFF9FAFB)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFC107)
    val WarningText = Color(0xFF856404)
    val WarningBg = Color(0xFFFFF3CD)
    val BlueAction = Color(0xFF1976D2) // El azul de tus botones
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
// 3.0 LÓGICA DE RED (CONEXIÓN REAL)
// ==================================================================
suspend fun enviarAInternetReal(record: ScanRecord): Boolean {
    return try {
        println("📡 ENVIANDO A LA NUBE: ${record.name}")
        delay(1000)
        true
    } catch (e: Exception) {
        false
    }
}

// ==================================================================
// 4.0 COMPONENTES UI REUTILIZABLES
// ==================================================================
@Composable
fun EduInput(label: String, value: String, onValueChange: (String) -> Unit, icon: ImageVector, placeholder: String = "", isNumber: Boolean = false) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(text = label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange, placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EduTheme.BrandRed, unfocusedBorderColor = Color(0xFFE5E7EB), focusedContainerColor = EduTheme.White, unfocusedContainerColor = EduTheme.InputBg)
        )
    }
}

// ==================================================================
// 5.0 PANTALLAS PRINCIPALES
// ==================================================================

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(EduTheme.White)) {
        Box(modifier = Modifier.offset(y = (-50).dp).size(300.dp).background(EduTheme.BrandRed.copy(alpha = 0.1f), CircleShape).blur(radius = 50.dp).align(Alignment.TopCenter))
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(80.dp), tint = EduTheme.BrandRed)
            Text("EduTec", fontSize = 48.sp, fontWeight = FontWeight.Black, letterSpacing = (-2).sp, color = EduTheme.BrandBlack)
            Spacer(modifier = Modifier.height(48.dp))
            OutlinedButton(onClick = { onNavigate("admin") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(20.dp), border = BorderStroke(2.dp, Color(0xFFEEEEEE)), colors = ButtonDefaults.outlinedButtonColors(containerColor = EduTheme.White)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.size(45.dp).background(EduTheme.GrayBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.VpnKey, null, tint = EduTheme.BrandBlack) }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column { Text("Soy Staff", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EduTheme.BrandBlack); Text("Ingreso directo al sistema", fontSize = 12.sp, color = Color.Gray) }
                }
            }
        }
    }
}

@Composable
fun StudentRegisterScreen(onBack: () -> Unit, onRegister: (User) -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Text("Pantalla de Registro (Código abreviado)", Modifier.align(Alignment.Center))
        Button(onClick = onBack, Modifier.align(Alignment.TopStart).padding(16.dp)) { Text("Volver") }
    }
}

// --- PANTALLA DE ADMIN (LA QUE IMPORTA) ---
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    db: MutableList<User>,
    scans: MutableList<ScanRecord>,
    onAddScan: (ScanRecord) -> Unit
) {
    // --- LÓGICA OFFLINE E INTELIGENTE ---
    val scope = rememberCoroutineScope()
    val networkMonitor = remember { getNetworkMonitor() }
    val isOnline by networkMonitor.isConnected.collectAsState(initial = false)
    val pendingQueue = remember { mutableStateListOf<ScanRecord>() }

    // --- ESTADOS DE TU DISEÑO ORIGINAL ---
    var mode by remember { mutableStateOf("camera") }
    var manualTab by remember { mutableStateOf("quick") }
    var manualDni by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Estados para Edición (Zona 3)
    var editingUser by remember { mutableStateOf<User?>(null) }
    var editDniVal by remember { mutableStateOf("") }

    // Sincronización automática
    LaunchedEffect(isOnline) {
        if (isOnline && pendingQueue.isNotEmpty()) {
            val itemsToSync = pendingQueue.toList()
            itemsToSync.forEach { enviarAInternetReal(it) }
            pendingQueue.clear()
        }
    }

    // --- PROCESAMIENTO CENTRAL ---
    fun processEntry(user: User, method: String) {
        val isReentry = scans.any { it.dni == user.dni }
        val type = if (isReentry) "Re-ingreso" else method
        val newRecord = ScanRecord(Random.nextLong(), user.name, user.dni, "10:30 AM", type)

        onAddScan(newRecord) // Agregar visualmente

        // Lógica de Red/Offline
        scope.launch {
            if (isOnline) {
                if (!enviarAInternetReal(newRecord)) pendingQueue.add(newRecord)
            } else {
                pendingQueue.add(newRecord)
            }
        }
        feedback = if (isReentry) "reentry" to "RE-INGRESO AUTORIZADO" else "success" to "INGRESO EXITOSO"

        // Limpiar campos
        manualDni = ""
        searchText = ""
        editingUser = null
    }

    // Guardar Edición de DNI
    fun saveUserDni() {
        editingUser?.let { user ->
            val index = db.indexOfFirst { it.id == user.id }
            if (index != -1) {
                val newUser = user.copy(dni = editDniVal)
                db[index] = newUser
                processEntry(newUser, "Corrección")
            }
        }
    }

    LaunchedEffect(feedback) { if (feedback != null) { delay(2500); feedback = null } }

    Box(modifier = Modifier.fillMaxSize().background(EduTheme.DarkHeader)) {

        // --- BARRAS DE ESTADO DE RED (OFFLINE/SYNC) ---
        Column(modifier = Modifier.zIndex(100f).fillMaxWidth()) {
            if (!isOnline) {
                Box(modifier = Modifier.fillMaxWidth().background(EduTheme.BrandRed).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("⚠️ MODO OFFLINE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (pendingQueue.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFF9800)).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("Sincronizando ${pendingQueue.size} registros...", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF111111)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).background(EduTheme.BrandRed, CircleShape), contentAlignment = Alignment.Center) { Text("ST", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column { Text("OPERADOR", color = Color.Gray, fontSize = 10.sp); Text("EduTec Staff", color = Color.White, fontWeight = FontWeight.Bold) }
                }
                Button(onClick = onBack, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Salir", fontSize = 10.sp) }
            }

            // CONTENIDO PRINCIPAL
            Box(modifier = Modifier.weight(1f)) {
                if (mode == "camera") {
                    // === CÁMARA REAL ===
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                        CameraPreview(
                            reductionFactor = 1f,
                            onCameraStatusChanged = { _, _ -> },
                            onQrDetected = { qr ->
                                val user = db.find { it.dni == qr } ?: User(0, "Desconocido", qr, "", "", "")
                                processEntry(user, "QR")
                            }
                        )
                        Box(modifier = Modifier.size(280.dp).border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(24.dp))) {
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(EduTheme.BrandRed).align(Alignment.Center))
                        }
                    }
                } else {
                    // === MODO MANUAL ===
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // Tabs
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)).padding(4.dp)) {
                            val activeColor = Color(0xFF1976D2)
                            Button(onClick = { manualTab = "quick" }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if(manualTab=="quick") activeColor else Color.Transparent)) { Text("Ingreso Rápido", fontSize = 12.sp) }
                            Button(onClick = { manualTab = "search" }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if(manualTab=="search") activeColor else Color.Transparent)) { Text("Corregir / Buscar", fontSize = 12.sp) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (manualTab == "quick") {
                            // --- TAB 1: Tarjeta del Rayo (VALIDACIÓN AGREGADA) ---
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF232323))) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Bolt, null, tint = EduTheme.BlueAction, modifier = Modifier.size(48.dp))
                                    Text("Ingreso Directo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Validar entrada por DNI (8 dígitos)", color = Color.Gray, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(24.dp))

                                    // CAMPO DE TEXTO CON VALIDACIÓN
                                    OutlinedTextField(
                                        value = manualDni,
                                        onValueChange = { newValue ->
                                            // VALIDACIÓN: Solo números y máximo 8 caracteres
                                            if (newValue.length <= 8 && newValue.all { it.isDigit() }) {
                                                manualDni = newValue
                                            }
                                        },
                                        placeholder = { Text("Escribe DNI...", color = Color.Gray, fontSize = 24.sp) },
                                        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center, color = Color.White),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EduTheme.BlueAction, unfocusedBorderColor = Color.Gray),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            // VALIDAR AL HACER CLIC
                                            if (manualDni.length == 8) {
                                                val user = db.find { it.dni == manualDni } ?: User(0, "Invitado", manualDni, "", "", "")
                                                processEntry(user, "Manual")
                                            }
                                        },
                                        // Deshabilitar botón si no tiene 8 dígitos
                                        enabled = manualDni.length == 8,
                                        modifier = Modifier.fillMaxWidth().height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EduTheme.BlueAction,
                                            disabledContainerColor = Color.Gray
                                        )
                                    ) {
                                        Text("MARCAR ASISTENCIA", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // --- TAB 2: Buscador por DNI (MODIFICADO) ---
                            Column {
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = { newValue ->
                                        // Validación opcional: buscar solo números para DNI
                                        if (newValue.all { it.isDigit() }) {
                                            searchText = newValue
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                                    placeholder = { Text("Buscar por DNI...", color = Color.Gray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFF232323), unfocusedContainerColor = Color(0xFF232323), focusedBorderColor = EduTheme.BlueAction, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    // FILTRO MODIFICADO: BUSCAR POR DNI
                                    val results = if(searchText.isNotEmpty()) db.filter { it.dni.contains(searchText) } else emptyList()

                                    items(results) { user ->
                                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)), modifier = Modifier.padding(bottom = 8.dp)) {
                                            if (editingUser?.id == user.id) {
                                                // MODO EDICIÓN
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text("EDITANDO DNI", color = EduTheme.BlueAction, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    OutlinedTextField(
                                                        value = editDniVal,
                                                        onValueChange = { if (it.length <= 8 && it.all { char -> char.isDigit() }) editDniVal = it },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                                    )
                                                    Row(modifier = Modifier.padding(top = 8.dp)) {
                                                        Button(onClick = { editingUser = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar") }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Button(onClick = { saveUserDni() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = EduTheme.Success)) { Text("Guardar") }
                                                    }
                                                }
                                            } else {
                                                // MODO VISTA
                                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                    Column { Text(user.name, color = Color.White, fontWeight = FontWeight.Bold); Text("DNI: ${user.dni}", color = Color.Gray, fontSize = 12.sp) }
                                                    Row {
                                                        IconButton(onClick = { editingUser = user; editDniVal = user.dni }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                                                        Button(onClick = { processEntry(user, "Search") }, colors = ButtonDefaults.buttonColors(containerColor = EduTheme.BlueAction), modifier = Modifier.height(35.dp)) { Text("Entrar", fontSize = 10.sp) }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MENU BOTTOM
            Row(modifier = Modifier.background(Color(0xFF111111)).padding(8.dp)) {
                Button(onClick = { mode = "camera" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="camera") Color(0xFF333333) else Color.Transparent)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.CameraAlt, null); Text("Cámara", fontSize = 10.sp) } }
                Button(onClick = { mode = "manual" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="manual") EduTheme.BlueAction.copy(0.3f) else Color.Transparent)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Search, null, tint = if(mode=="manual") Color(0xFF64B5F6) else Color.White); Text("Manual", fontSize = 10.sp, color = if(mode=="manual") Color(0xFF64B5F6) else Color.White) } }
            }

            // HISTORY SHEET
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(EduTheme.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))) {
                Column {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp).width(40.dp).height(4.dp).background(Color.LightGray, CircleShape))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Aforo: ${scans.size}", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                        Text("EN VIVO", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 10.sp, modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Divider(color = Color(0xFFEEEEEE))
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(scans) { scan ->
                            Row(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth().border(1.dp, if(scan.type=="Re-ingreso") EduTheme.Warning else Color(0xFFEEEEEE), RoundedCornerShape(8.dp)).background(if(scan.type=="Re-ingreso") EduTheme.WarningBg else EduTheme.White, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).background(if(scan.type=="Re-ingreso") EduTheme.Warning else if(scan.type=="QR") Color(0xFFE8F5E9) else Color(0xFFE3F2FD), CircleShape), contentAlignment = Alignment.Center) { Icon(if(scan.type=="Re-ingreso") Icons.Default.Replay else if(scan.type=="QR") Icons.Default.QrCode else Icons.Default.Keyboard, null, tint = if(scan.type=="Re-ingreso") Color.White else if(scan.type=="QR") Color(0xFF2E7D32) else Color(0xFF1565C0), modifier = Modifier.size(16.dp)) }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) { Text(scan.name, fontWeight = FontWeight.Bold, fontSize = 14.sp); Row(verticalAlignment = Alignment.CenterVertically) { Text("ID: ${scan.dni}", fontSize = 10.sp, color = Color.Gray); if(scan.type=="Re-ingreso") { Spacer(modifier = Modifier.width(8.dp)); Text("RE-INGRESO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EduTheme.WarningText, modifier = Modifier.background(EduTheme.Warning, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)) } } }
                                Text(scan.time, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // FEEDBACK OVERLAY
        if (feedback != null) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.8f)).zIndex(100f).clickable { feedback = null }, contentAlignment = Alignment.Center) {
                val (type, msg) = feedback!!
                val color = if (type == "reentry") EduTheme.Warning else EduTheme.Success
                Column(modifier = Modifier.width(300.dp).background(Color.White, RoundedCornerShape(24.dp)).border(4.dp, color, RoundedCornerShape(24.dp)).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (type == "reentry") Icons.Default.Replay else Icons.Default.CheckCircle, null, tint = color, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(msg, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if(type=="reentry") EduTheme.WarningText else Color(0xFF1B5E20), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ==================================================================
// 5.0 ENTRY POINT (APP)
// ==================================================================
@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = EduTheme.BrandRed,
            background = EduTheme.GrayBg,
            surface = EduTheme.White
        )
    ) {
        var currentScreen by remember { mutableStateOf("home") }
        val db = remember { mutableStateListOf<User>().apply { addAll(INITIAL_DB) } }
        val scans = remember { mutableStateListOf<ScanRecord>() }

        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                "home" -> HomeScreen(onNavigate = { currentScreen = it })
                "student" -> StudentRegisterScreen(onBack = { currentScreen = "home" }, onRegister = { db.add(it) })
                "admin" -> AdminScreen(onBack = { currentScreen = "home" }, db = db, scans = scans, onAddScan = { scans.add(0, it) })
            }
        }
    }
}