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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import frontapp.composeapp.generated.resources.Res
import frontapp.composeapp.generated.resources.logo_app
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource

// Manejo del botón atrás nativo
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

// ==================================================================
// 1. SISTEMA DE DISEÑO MEJORADO
// ==================================================================
object EduTheme {
    val BrandRed = Color(0xFFCF0A2C)
    val BrandBlack = Color(0xFF1A1A1A)
    val DarkHeader = Color(0xFF121212)
    val White = Color(0xFFFFFFFF)
    val GrayBg = Color(0xFFF8F9FA)
    val CardBg = Color(0xFFFFFFFF)
    val Success = Color(0xFF2E7D32)
    val Warning = Color(0xFFF57C00)
    val WarningBg = Color(0xFFFFF9E6)
    val BlueAction = Color(0xFF1565C0)
    val Error = Color(0xFFC62828)
    val TextMain = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
}

// ==================================================================
// 2. MODELOS DE DATOS
// ==================================================================
data class User(
    val id: String, 
    val name: String, 
    val dni: String, 
    val email: String, 
    val school: String, 
    val phone: String,
    val position: String = "Sin Cargo",
    val company: String = "Sin Institución"
)

// ==================================================================
// 3. PANTALLAS
// ==================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var historyList by remember { mutableStateOf<List<AsistenciaResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    BackHandler { onBack() }

    fun formatPeruTime(isoString: String?): String {
        if (isoString == null) return "--:--"
        return try {
            val instant = Instant.parse(isoString)
            val peruZone = TimeZone.of("America/Lima")
            val localDateTime = instant.toLocalDateTime(peruZone)
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val min = localDateTime.minute.toString().padStart(2, '0')
            val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
            val month = localDateTime.monthNumber.toString().padStart(2, '0')
            "$day/$month $hour:$min"
        } catch (e: Exception) { "Error" }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            historyList = EduTecApi.obtenerHistorial()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Historial", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Registros de asistencia", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.White.copy(0.7f))
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBackIosNew, "Volver", modifier = Modifier.size(20.dp)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = EduTheme.DarkHeader, titleContentColor = Color.White, navigationIconContentColor = Color.White),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = EduTheme.BrandRed) }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(EduTheme.GrayBg), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(historyList) { item ->
                    val isReentry = item.status?.uppercase() == "REINGRESO"
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = if (isReentry) EduTheme.WarningBg else Color.White)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(50.dp).background(if (isReentry) Color(0xFFFFECB3) else EduTheme.Success.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(if (isReentry) Icons.Default.History else Icons.Default.Person, null, tint = if (isReentry) EduTheme.Warning else EduTheme.Success, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = EduTheme.TextMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("DNI: ${item.dni}", fontSize = 12.sp, color = EduTheme.TextSecondary)
                                    if (isReentry) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(color = EduTheme.Warning, shape = RoundedCornerShape(6.dp)) { Text("RE-IN", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White) }
                                    }
                                }
                                
                                val posText = item.position ?: item.cargo
                                val compText = item.company ?: item.institucion

                                if (!posText.isNullOrBlank() || !compText.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        if (!posText.isNullOrBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Work, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                                                Text(" $posText", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        if (!compText.isNullOrBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Business, null, modifier = Modifier.size(10.dp), tint = Color.Gray)
                                                Text(" $compText", color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatPeruTime(item.createdAt), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EduTheme.TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(if (isReentry) "DUPLICADO" else "INGRESÓ", fontSize = 11.sp, fontWeight = FontWeight.Black, color = if (isReentry) EduTheme.Warning else EduTheme.Success)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit, db: MutableList<User>, onNavigateToHistory: () -> Unit) {
    val scope = rememberCoroutineScope()
    val networkMonitor = remember { getNetworkMonitor() }
    val isOnline by networkMonitor.isConnected.collectAsState(initial = false)

    BackHandler { onBack() }

    var mode by remember { mutableStateOf("camera") }
    var manualTab by remember { mutableStateOf("quick") }
    var manualDni by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var editDniVal by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.Default) {
            try {
                val usersApi = EduTecApi.obtenerUsuarios()
                withContext(Dispatchers.Main) {
                    db.clear()
                    db.addAll(usersApi.map { 
                        User(
                            id = it.id, 
                            name = it.fullName, 
                            dni = it.dni, 
                            email = "", 
                            school = "", 
                            phone = "",
                            position = (it.position ?: it.cargo) ?: "Sin Cargo",
                            company = (it.company ?: it.institucion) ?: "Sin Institución"
                        ) 
                    })
                }
            } catch (e: Exception) {}
        }
    }

    fun processEntry(user: User, method: String) {
        scope.launch {
            if (isOnline) {
                val result = if (method == "QR") EduTecApi.registrarConNombre(user.dni, user.name) else EduTecApi.registrarPorDni(user.dni)
                
                // CORRECCIÓN: Manejar el objeto Result correctamente
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    feedback = (data?.status?.lowercase() ?: "success") to (data?.message ?: "¡BIENVENIDO!")
                } else {
                    feedback = "error" to (result.exceptionOrNull()?.message ?: "ERROR DE SERVIDOR")
                }
            } else { feedback = "error" to "SIN CONEXIÓN A INTERNET" }
        }
    }

    fun saveUserDniCorrection() {
        editingUser?.let { user ->
            scope.launch {
                if (isOnline && editDniVal.length == 8) {
                    val exito = EduTecApi.corregirUsuario(user.id, editDniVal)
                    if (exito) {
                        val index = db.indexOfFirst { it.id == user.id }
                        if (index != -1) db[index] = db[index].copy(dni = editDniVal)
                        feedback = "success" to "DNI CORREGIDO CORRECTAMENTE"
                        editingUser = null
                    } else { feedback = "error" to "ERROR AL GUARDAR CAMBIOS" }
                }
            }
        }
    }

    LaunchedEffect(feedback) { if (feedback != null) { delay(2500); feedback = null; isProcessing = false } }

    Box(modifier = Modifier.fillMaxSize().background(EduTheme.DarkHeader)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("EduTec Staff", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(if(isOnline) Color.Green else Color.Red, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if(isOnline) "En línea" else "Sin conexión", color = Color.White.copy(0.5f), fontSize = 11.sp)
                    }
                }
                Row {
                    IconButton(onClick = onNavigateToHistory, modifier = Modifier.background(Color.White.copy(0.1f), CircleShape)) { Icon(Icons.Default.Group, null, tint = Color.White) }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onBack, modifier = Modifier.background(EduTheme.BrandRed.copy(0.2f), CircleShape)) { Icon(Icons.Default.Logout, null, tint = EduTheme.BrandRed) }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).background(EduTheme.GrayBg)) {
                if (mode == "camera") {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        CameraPreview(reductionFactor = 1f, onCameraStatusChanged = { _, _ -> }, onQrDetected = { qrContent ->
                            if (isProcessing) return@CameraPreview
                            isProcessing = true
                            scope.launch(Dispatchers.Default) {
                                var extractedDni: String? = null
                                var extractedName = "Invitado"
                                try {
                                    val data = Json.decodeFromString<AsistenciaRegisterRequest>(qrContent)
                                    extractedDni = data.dni
                                    extractedName = data.fullName
                                } catch (e: Exception) { if (qrContent.length == 8 && qrContent.all { it.isDigit() }) extractedDni = qrContent }
                                withContext(Dispatchers.Main) {
                                    if (extractedDni != null) processEntry(User("0", extractedName, extractedDni, "", "", ""), "QR")
                                    else feedback = "error" to "CÓDIGO QR NO VÁLIDO"
                                }
                            }
                        })
                        val scanAreaSize = minOf(maxWidth, maxHeight) * 0.7f
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(scanAreaSize).border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(24.dp))) {
                                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, EduTheme.BrandRed, Color.Transparent))).align(Alignment.Center))
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                        TabRow(selectedTabIndex = if(manualTab == "quick") 0 else 1, containerColor = Color.Transparent, contentColor = EduTheme.BlueAction, indicator = { tabPositions -> TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[if(manualTab == "quick") 0 else 1]), color = EduTheme.BlueAction, height = 3.dp) }, divider = {}) {
                            Tab(selected = manualTab == "quick", onClick = { manualTab = "quick" }) { Text("REGISTRO RÁPIDO", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            Tab(selected = manualTab == "search", onClick = { manualTab = "search" }) { Text("BUSCADOR", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        if (manualTab == "quick") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Ingresa el DNI para marcar ingreso", color = EduTheme.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(32.dp))
                                OutlinedTextField(value = manualDni, onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) manualDni = it }, placeholder = { Text("DNI (8 dígitos)", color = Color.LightGray) }, textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Black), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EduTheme.BlueAction, unfocusedBorderColor = Color.LightGray))
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(onClick = { if (manualDni.length == 8) { val user = db.find { it.dni == manualDni } ?: User("0", "Invitado", manualDni, "", "", ""); processEntry(user, "Manual") }}, enabled = manualDni.length == 8, modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = EduTheme.BlueAction)) { Text("MARCAR ASISTENCIA", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                            }
                        } else {
                            OutlinedTextField(value = searchText, onValueChange = { if(it.all {c->c.isDigit()}) searchText = it }, leadingIcon = { Icon(Icons.Default.Search, null, tint = EduTheme.BlueAction) }, placeholder = { Text("Buscar por DNI...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val results = if(searchText.isNotEmpty()) db.filter { it.dni.contains(searchText) } else db
                                items(results) { user ->
                                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(1.dp)) {
                                        if (editingUser?.id == user.id) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text("Corregir DNI", fontWeight = FontWeight.Bold, color = EduTheme.BlueAction)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(value = editDniVal, onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) editDniVal = it }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                                Row(modifier = Modifier.padding(top = 12.dp)) {
                                                    TextButton(onClick = { editingUser = null }) { Text("Cancelar", color = Color.Gray) }
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Button(onClick = { saveUserDniCorrection() }, colors = ButtonDefaults.buttonColors(containerColor = EduTheme.Success)) { Text("Guardar") }
                                                }
                                            }
                                        } else {
                                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(user.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text("DNI: ${user.dni}", color = EduTheme.TextSecondary, fontSize = 12.sp)
                                                    Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Work, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                                            Text(" ${user.position}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Business, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                                            Text(" ${user.company}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                                IconButton(onClick = { editingUser = user; editDniVal = user.dni }) { Icon(Icons.Default.Edit, null, tint = Color.LightGray, modifier = Modifier.size(18.dp)) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Surface(color = EduTheme.White, shadowElevation = 8.dp) {
                Row(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(8.dp)) {
                    Button(onClick = { mode = "camera" }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="camera") EduTheme.BrandRed else Color.Transparent), shape = RoundedCornerShape(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.QrCodeScanner, null, tint = if(mode=="camera") Color.White else Color.Gray); Spacer(modifier = Modifier.width(8.dp)); Text("ESCANEAR", color = if(mode=="camera") Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { mode = "manual" }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = if(mode=="manual") EduTheme.BlueAction else Color.Transparent), shape = RoundedCornerShape(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.EditNote, null, tint = if(mode=="manual") Color.White else Color.Gray); Spacer(modifier = Modifier.width(8.dp)); Text("MANUAL", color = if(mode=="manual") Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }
                }
            }
        }
        if (feedback != null) {
            val (type, msg) = feedback!!
            val isError = type == "error"; val isWarning = type == "reingreso" || type == "reentry"
            val color = when { isError -> EduTheme.Error; isWarning -> EduTheme.Warning; else -> EduTheme.Success }
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.75f)).zIndex(200f).clickable { feedback = null; isProcessing = false }, contentAlignment = Alignment.Center) {
                Card(modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) { Box(contentAlignment = Alignment.Center) { Icon(imageVector = when { isError -> Icons.Default.Cancel; isWarning -> Icons.Default.PriorityHigh; else -> Icons.Default.CheckCircle }, contentDescription = null, tint = color, modifier = Modifier.size(44.dp)) } }
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(text = msg, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = color, lineHeight = 28.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = when { isError -> "Ocurrió un error al procesar la solicitud. Intenta de nuevo."; isWarning -> "Este usuario ya cuenta con un registro previo en el sistema."; else -> "El ingreso ha sido registrado correctamente." }, fontSize = 14.sp, color = EduTheme.TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(EduTheme.White).statusBarsPadding()) {
        Box(modifier = Modifier.offset(x = 100.dp, y = (-100).dp).size(400.dp).background(EduTheme.BlueAction.copy(0.05f), CircleShape).blur(80.dp))
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val logo_app = painterResource(Res.drawable.logo_app)
            Surface(modifier = Modifier.size(120.dp), shape = RoundedCornerShape(30.dp), color = EduTheme.BlueAction) { Box(contentAlignment = Alignment.Center) { Image(painter = logo_app, contentDescription = "Grupo Upgrade", modifier = Modifier.size(60.dp)) } }
            Spacer(modifier = Modifier.height(32.dp))
            Text("EduTec", fontSize = 48.sp, fontWeight = FontWeight.Black, color = EduTheme.BrandBlack, letterSpacing = (-2).sp)
            Text("Gestión de Eventos & Asistencia", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Spacer(modifier = Modifier.height(60.dp))
            Button(onClick = { onNavigate("admin") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = EduTheme.BrandBlack), elevation = ButtonDefaults.buttonElevation(8.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.QrCodeScanner, null); Spacer(modifier = Modifier.width(16.dp)); Column(horizontalAlignment = Alignment.Start) { Text("ACCESO STAFF", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp); Text("Iniciar escaneo de QR", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color.White.copy(0.6f)) }; Spacer(modifier = Modifier.weight(1f)); Icon(Icons.Default.ArrowForward, null) } }
        }
        Text("v1.0.2 • Grupo Upgrade", modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).navigationBarsPadding(), fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun App() {
    MaterialTheme(colorScheme = lightColorScheme(primary = EduTheme.BrandRed, onPrimary = Color.White, surface = EduTheme.White, background = EduTheme.GrayBg)) {
        var currentScreen by remember { mutableStateOf("home") }
        val db = remember { mutableStateListOf<User>() }
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (currentScreen) {
                "home" -> HomeScreen(onNavigate = { currentScreen = it })
                "admin" -> AdminScreen(onBack = { currentScreen = "home" }, db = db, onNavigateToHistory = { currentScreen = "history" })
                "history" -> HistoryScreen(onBack = { currentScreen = "admin" })
            }
        }
    }
}
