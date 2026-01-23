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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.random.Random

// ==================================================================
// 1. SISTEMA DE DISEÑO
// ==================================================================
object EduTheme {
    val BrandRed = Color(0xFFCF0A2C)
    val BrandRedDark = Color(0xFF8B0000)
    val BrandBlack = Color(0xFF232323)
    val DarkHeader = Color(0xFF1a1a1a)
    val White = Color(0xFFFFFFFF)
    val GrayBg = Color(0xFFF2F2F2)
    val InputBg = Color(0xFFF9FAFB)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFC107)
    val WarningText = Color(0xFF856404)
    val WarningBg = Color(0xFFFFF3CD)
    val BlueAction = Color(0xFF1976D2)
    val Error = Color(0xFFD32F2F)
}

// ==================================================================
// 2. MODELOS DE DATOS
// ==================================================================
data class User(val id: String, val name: String, val dni: String, val email: String, val school: String, val phone: String)
data class ScanRecord(val id: String, val name: String, val dni: String, val time: String, val type: String, val status: String = "INGRESO")

// ==================================================================
// 3. PANTALLAS
// ==================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    db: MutableList<User>,
    scans: MutableList<ScanRecord>
) {
    val scope = rememberCoroutineScope()
    val networkMonitor = remember { getNetworkMonitor() }
    val isOnline by networkMonitor.isConnected.collectAsState(initial = false)
    val pendingQueue = remember { mutableStateListOf<ScanRecord>() }

    var mode by remember { mutableStateOf("camera") }
    var manualTab by remember { mutableStateOf("quick") }
    var manualDni by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    // Control de flujo para evitar bloqueos
    var isProcessing by remember { mutableStateOf(false) }

    var editingUser by remember { mutableStateOf<User?>(null) }
    var editDniVal by remember { mutableStateOf("") }

    val scaffoldState = rememberBottomSheetScaffoldState()

    // REFRESH OPTIMIZADO: Ejecutado en background
    fun refreshHistoryFromServer() {
        scope.launch(Dispatchers.Default) {
            try {
                val historyApi = EduTecApi.obtenerHistorial()
                val newList = historyApi.map { 
                    ScanRecord(
                        id = Random.nextLong().toString(), 
                        name = it.fullName, 
                        dni = it.dni, 
                        time = "Ahora", 
                        type = "API",
                        status = it.status ?: "INGRESO"
                    ) 
                }
                withContext(Dispatchers.Main) {
                    if (scans.size != newList.size) {
                        scans.clear()
                        scans.addAll(newList)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.Default) {
            try {
                val usersApi = EduTecApi.obtenerUsuarios()
                withContext(Dispatchers.Main) {
                    db.clear()
                    db.addAll(usersApi.map { User(it.id, it.fullName, it.dni, "", "", "") })
                }
            } catch (e: Exception) {}
        }
        refreshHistoryFromServer()
    }

    // REGISTRO OPTIMIZADO
    fun processEntry(user: User, method: String) {
        scope.launch {
            if (isOnline) {
                // Si es QR, enviamos DNI y Nombre para validación completa
                val result = if (method == "QR") {
                    EduTecApi.registrarConNombre(user.dni, user.name)
                } else {
                    EduTecApi.registrarPorDni(user.dni)
                }

                if (result.isSuccess) {
                    val data = result.getOrNull()
                    refreshHistoryFromServer()
                    feedback = (data?.status?.lowercase() ?: "success") to (data?.message ?: "¡BIENVENIDO!")
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "ERROR"
                    feedback = "error" to errorMsg
                }
            } else {
                pendingQueue.add(ScanRecord(Random.nextLong().toString(), user.name, user.dni, "Sync", method, "INGRESO"))
                feedback = "success" to "GUARDADO OFFLINE"
            }
        }
    }

    // GUARDAR CORRECCIÓN
    fun saveUserDniCorrection() {
        editingUser?.let { user ->
            scope.launch {
                if (isOnline && editDniVal.length == 8) {
                    val exito = EduTecApi.corregirUsuario(user.id, editDniVal)
                    if (exito) {
                        val index = db.indexOfFirst { it.id == user.id }
                        if (index != -1) db[index] = db[index].copy(dni = editDniVal)
                        feedback = "success" to "DNI CORREGIDO"
                        editingUser = null
                    } else {
                        feedback = "error" to "ERROR AL GUARDAR"
                    }
                }
            }
        }
    }

    // RESET DE ESTADO RÁPIDO
    LaunchedEffect(feedback) { 
        if (feedback != null) { 
            delay(1500) // Feedback más corto para agilidad
            feedback = null 
            isProcessing = false
        } 
    }

    Box(modifier = Modifier.fillMaxSize().background(EduTheme.DarkHeader)) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 180.dp,
            sheetContainerColor = EduTheme.White,
            containerColor = EduTheme.DarkHeader,
            sheetContent = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(min = 400.dp)) {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp).width(40.dp).height(4.dp).background(Color.LightGray, CircleShape))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Aforo: ${scans.size}", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                        Text("EN VIVO", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 10.sp, modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Divider(color = Color(0xFFEEEEEE))
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(scans, key = { it.id }) { scan ->
                            val isReentry = scan.status == "REINGRESO"
                            val bgColor = if (isReentry) EduTheme.WarningBg else EduTheme.White
                            val borderColor = if (isReentry) EduTheme.Warning else Color(0xFFEEEEEE)
                            val iconBg = if (isReentry) EduTheme.Warning.copy(alpha = 0.2f) else Color(0xFFE8F5E9)
                            val iconColor = if (isReentry) EduTheme.WarningText else Color(0xFF2E7D32)

                            Row(modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(8.dp)).background(bgColor, RoundedCornerShape(8.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) { 
                                    Icon(if(isReentry) Icons.Default.History else Icons.Default.Check, null, tint = iconColor, modifier = Modifier.size(16.dp)) 
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) { 
                                    Text(scan.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("DNI: ${scan.dni}", fontSize = 10.sp, color = Color.Gray)
                                        if (isReentry) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("RE-INGRESO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EduTheme.WarningText, modifier = Modifier.background(EduTheme.Warning, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text(if(isReentry) "RE-IN" else "OK", fontSize = 10.sp, color = iconColor, fontWeight = FontWeight.Bold)
                            }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF111111)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(EduTheme.BrandRed, CircleShape), contentAlignment = Alignment.Center) { Text("ST", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EduTec Staff", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onBack, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Salir", fontSize = 10.sp) }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (mode == "camera") {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                            CameraPreview(
                                reductionFactor = 1f,
                                onCameraStatusChanged = { _, _ -> },
                                onQrDetected = { qrContent ->
                                    if (isProcessing) return@CameraPreview
                                    isProcessing = true // Bloqueo instantáneo

                                    scope.launch(Dispatchers.Default) {
                                        var extractedDni: String? = null
                                        var extractedName = "Invitado"
                                        try {
                                            val data = Json.decodeFromString<AsistenciaRegisterRequest>(qrContent)
                                            extractedDni = data.dni
                                            extractedName = data.fullName
                                        } catch (e: Exception) {
                                            if (qrContent.length == 8 && qrContent.all { it.isDigit() }) {
                                                extractedDni = qrContent
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            if (extractedDni != null) {
                                                // Enviamos siempre el nombre extraído del QR para que el backend valide
                                                processEntry(User("0", extractedName, extractedDni, "", "", ""), "QR")
                                            } else {
                                                feedback = "error" to "QR NO RECONOCIDO"
                                            }
                                        }
                                    }
                                }
                            )
                            Box(modifier = Modifier.size(280.dp).border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(24.dp))) { Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(EduTheme.BrandRed).align(Alignment.Center)) }
                        }
                    } else {
                        // Modo manual
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)).padding(4.dp)) {
                                Button(onClick = { manualTab = "quick" }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if(manualTab=="quick") EduTheme.BlueAction else Color.Transparent)) { Text("Ingreso Rápido", fontSize = 12.sp) }
                                Button(onClick = { manualTab = "search" }, modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = if(manualTab=="search") EduTheme.BlueAction else Color.Transparent)) { Text("Corregir / Buscar", fontSize = 12.sp) }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (manualTab == "quick") {
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF232323))) {
                                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Bolt, null, tint = EduTheme.BlueAction, modifier = Modifier.size(48.dp))
                                        Text("Ingreso Directo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        OutlinedTextField(
                                            value = manualDni,
                                            onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) manualDni = it },
                                            placeholder = { Text("DNI...", color = Color.Gray, fontSize = 24.sp) },
                                            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center, color = Color.White),
                                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EduTheme.BlueAction, unfocusedBorderColor = Color.Gray),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { if (manualDni.length == 8) {
                                            val user = db.find { it.dni == manualDni } ?: User("0", "Invitado", manualDni, "", "", "")
                                            processEntry(user, "Manual")
                                        }}, enabled = manualDni.length == 8, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = EduTheme.BlueAction)) { Text("MARCAR ASISTENCIA", fontWeight = FontWeight.Bold) }
                                    }
                                }
                            } else {
                                Column {
                                    OutlinedTextField(
                                        value = searchText,
                                        onValueChange = { if(it.all {c->c.isDigit()}) searchText = it },
                                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                                        placeholder = { Text("Buscar por DNI...", color = Color.Gray) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFF232323), unfocusedContainerColor = Color(0xFF232323), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyColumn(modifier = Modifier.weight(1f)) {
                                        val results = if(searchText.isNotEmpty()) db.filter { it.dni.contains(searchText) } else emptyList()
                                        items(results) { user ->
                                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)), modifier = Modifier.padding(bottom = 8.dp)) {
                                                if (editingUser?.id == user.id) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Text("CORREGIR DNI", color = EduTheme.BlueAction, fontSize = 10.sp)
                                                        OutlinedTextField(value = editDniVal, onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) editDniVal = it }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                                        Row(modifier = Modifier.padding(top = 8.dp)) {
                                                            Button(onClick = { editingUser = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar") }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Button(onClick = { saveUserDniCorrection() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = EduTheme.Success)) { Text("Guardar") }
                                                        }
                                                    }
                                                } else {
                                                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Column(modifier = Modifier.weight(1f)) { Text(user.name, color = Color.White, fontWeight = FontWeight.Bold); Text("DNI: ${user.dni}", color = Color.Gray, fontSize = 12.sp) }
                                                        Row {
                                                            IconButton(onClick = { editingUser = user; editDniVal = user.dni }) { Icon(Icons.Default.Edit, "Corregir", tint = Color.LightGray, modifier = Modifier.size(24.dp)) }
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

                Row(modifier = Modifier.background(Color(0xFF111111)).padding(8.dp)) {
                    Button(onClick = { mode = "camera" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="camera") Color(0xFF333333) else Color.Transparent)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.CameraAlt, null); Text("Cámara", fontSize = 10.sp) } }
                    Button(onClick = { mode = "manual" }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="manual") EduTheme.BlueAction.copy(0.3f) else Color.Transparent)) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Search, null, tint = if(mode=="manual") Color(0xFF64B5F6) else Color.White); Text("Manual", fontSize = 10.sp, color = if(mode=="manual") Color(0xFF64B5F6) else Color.White) } }
                }
            }
        }

        if (feedback != null) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.8f)).zIndex(150f).clickable { feedback = null; isProcessing = false }, contentAlignment = Alignment.Center) {
                val (type, msg) = feedback!!
                val isReingreso = type == "reingreso" || type == "reentry"
                val color = when {
                    isReingreso -> EduTheme.Warning
                    type == "error" -> EduTheme.Error
                    else -> EduTheme.Success
                }
                Column(modifier = Modifier.width(300.dp).background(Color.White, RoundedCornerShape(24.dp)).border(4.dp, color, RoundedCornerShape(24.dp)).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if(type=="error") Icons.Default.ErrorOutline else if(isReingreso) Icons.Default.History else Icons.Default.CheckCircle, 
                        null, 
                        tint = color, 
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        msg, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Black, 
                        color = if(type=="error") EduTheme.Error else if(isReingreso) EduTheme.WarningText else Color(0xFF1B5E20), 
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(EduTheme.White)) {
        Box(modifier = Modifier.offset(y = (-50).dp).size(300.dp).background(EduTheme.BrandRed.copy(alpha = 0.1f), CircleShape).blur(radius = 50.dp).align(Alignment.TopCenter))
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Rounded.QrCode2, contentDescription = null, modifier = Modifier.size(80.dp), tint = EduTheme.BrandRed)
            Text("EduTec", fontSize = 48.sp, fontWeight = FontWeight.Black, color = EduTheme.BrandBlack)
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
fun App() {
    MaterialTheme(colorScheme = lightColorScheme(primary = EduTheme.BrandRed, background = EduTheme.GrayBg, surface = EduTheme.White)) {
        var currentScreen by remember { mutableStateOf("home") }
        val db = remember { mutableStateListOf<User>() }
        val scans = remember { mutableStateListOf<ScanRecord>() }
        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                "home" -> HomeScreen(onNavigate = { currentScreen = it })
                "admin" -> AdminScreen(onBack = { currentScreen = "home" }, db = db, scans = scans)
            }
        }
    }
}
