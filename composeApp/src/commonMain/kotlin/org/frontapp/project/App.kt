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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer // A veces necesaria para efectos avanzados




// ==================================================================
// 1.0 SISTEMA DE DISEÑO (TOKENS & COLORES)
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
}

// ==================================================================
// 2.0 MODELOS DE DATOS
// ==================================================================
data class User(
    val id: Long,
    val name: String,
    val dni: String,
    val email: String,
    val school: String,
    val phone: String
)

data class ScanRecord(
    val id: Long,
    val name: String,
    val dni: String,
    val time: String,
    val type: String // "QR", "Manual", "Re-ingreso", "Search"
)

// Datos iniciales simulados
val INITIAL_DB = listOf(
    User(1, "Juan Perez", "12345678", "juan@test.com", "Universidad Nacional", "999888777"),
    User(2, "Maria Gomez", "87654321", "maria@test.com", "Instituto Tecnológico", "999111222")
)

// ==================================================================
// 3.0 COMPONENTES UI REUTILIZABLES
// ==================================================================

@Composable
fun EduInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    isNumber: Boolean = false
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EduTheme.BrandRed,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = EduTheme.White,
                unfocusedContainerColor = EduTheme.InputBg,
                cursorColor = EduTheme.BrandRed
            )
        )
    }
}

// ==================================================================
// 4.0 PANTALLAS PRINCIPALES
// ==================================================================

// --- 4.1 HOME SCREEN ---
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(EduTheme.White)) {
        // Decoración de fondo (Glow)
        Box(
            modifier = Modifier
                .offset(y = (-50).dp)
                .size(300.dp)
                .background(EduTheme.BrandRed.copy(alpha = 0.1f), CircleShape)
                .blur(radius = 50.dp)
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.QrCode2,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = EduTheme.BrandRed
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "EduTec",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp,
                color = EduTheme.BrandBlack
            )
            Text(
                "Tecnología Educativa 2026",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Text(
                "Powered by UPgrade & Huawei",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
/*
            // Botón Estudiante (Card Grande Roja)
            Card(
                onClick = { onNavigate("student") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EduTheme.BrandRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Regístrate Aquí", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Obtén tu pase GRATIS", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }*/

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Staff (Outline)
            OutlinedButton(
                onClick = { onNavigate("admin") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFEEEEEE)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = EduTheme.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(EduTheme.GrayBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = EduTheme.BrandBlack)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Soy Staff", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EduTheme.BrandBlack)
                        Text("Ingreso directo al sistema", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// --- 4.2 STUDENT REGISTRATION ---
@Composable
fun StudentRegisterScreen(onBack: () -> Unit, onRegister: (User) -> Unit) {
    var step by remember { mutableStateOf("form") }
    var showSavedToast by remember { mutableStateOf(false) }

    // Form Fields
    var name by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var school by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    fun submit() {
        step = "loading"
    }

    // Efecto de carga
    LaunchedEffect(step) {
        if (step == "loading") {
            delay(1500)
            onRegister(User(Random.nextLong(), name, dni, email, school, phone))
            step = "ticket"
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(EduTheme.White)) {
        // Botón flotante volver
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart).zIndex(10f)
                .background(if (step == "form") Color.White.copy(alpha = 0.2f) else EduTheme.GrayBg, CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = if(step=="form") Color.White else Color.Black)
        }

        if (step == "form") {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFA00822), EduTheme.BrandRed)
                            ),
                            shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.padding(bottom = 16.dp).background(Color.White.copy(0.1f), RoundedCornerShape(12.dp)).padding(8.dp)) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Text("EduTec 2026", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Innovación Educativa", fontSize = 16.sp, color = Color.White.copy(0.9f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ACCESO GRATUITO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.background(Color.Black.copy(0.2f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                }

                // Formulario
                Column(
                    modifier = Modifier
                        .offset(y = (-20).dp)
                        .padding(horizontal = 24.dp)
                        .background(EduTheme.White, RoundedCornerShape(24.dp))
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp))
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Registro de Asistente", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Completa tus datos para recibir tu QR.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp))

                    EduInput("Nombre Completo", name, { name = it }, Icons.Default.Person, "Ej: Juan Perez")
                    EduInput("DNI / Documento", dni, { dni = it }, Icons.Default.VpnKey, "Ej: 12345678", true)
                    EduInput("Correo Electrónico", email, { email = it }, Icons.Default.Email, "Ej: juan@mail.com")
                    EduInput("Teléfono", phone, { phone = it }, Icons.Default.Phone, "Ej: 999 888 777", true)
                    EduInput("Institución", school, { school = it }, Icons.Default.School, "Ej: Universidad UNI")

                    Button(
                        onClick = { submit() },
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EduTheme.BrandRed)
                    ) {
                        Text("Registrarme y Generar QR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (step == "loading") {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = EduTheme.BrandRed, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Procesando Registro", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Generando credencial digital...", color = Color.Gray)
            }
        } else {
            // TICKET VIEW
            Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp), contentAlignment = Alignment.TopCenter) {
                // Toast
                AnimatedVisibility(
                    visible = showSavedToast,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.padding(top = 16.dp).background(EduTheme.Success, CircleShape).padding(horizontal = 24.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("¡Guardado en Galería!", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("¡Hola, ${name.split(" ")[0]}!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Tu registro ha sido confirmado.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Ticket Card Visual
                    Card(
                        elevation = CardDefaults.cardElevation(20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = EduTheme.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Ticket Header
                            Box(modifier = Modifier.fillMaxWidth().height(80.dp).background(EduTheme.DarkHeader)) {
                                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("EDUTEC", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                                    Text("E-LEARNING 2026", color = EduTheme.BrandRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Ticket Body
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(160.dp).border(2.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.QrCode2, null, modifier = Modifier.size(140.dp))
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(name.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(school.uppercase(), fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)

                                Spacer(modifier = Modifier.height(16.dp))
                                Text("DNI: $dni", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(EduTheme.GrayBg, CircleShape).padding(horizontal = 16.dp, vertical = 6.dp))

                                // Dashed Line
                                Spacer(modifier = Modifier.height(24.dp))
                                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                                    drawLine(
                                        color = Color.LightGray,
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("FECHA", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("31 Ene", fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("HORA", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text("09:00 AM", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            showSavedToast = true
                            // Reset toast after 3 sec would require a LaunchedEffect here technically, simplified for now
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EduTheme.DarkHeader),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar en Galería")
                    }
                }
            }
        }
    }
}

// --- 4.3 ADMIN SCREEN (SCANNER & DASHBOARD) ---
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    db: MutableList<User>,
    scans: MutableList<ScanRecord>,
    onAddScan: (ScanRecord) -> Unit
) {
    var mode by remember { mutableStateOf("camera") } // camera, manual
    var manualTab by remember { mutableStateOf("quick") } // quick, search
    var feedback by remember { mutableStateOf<Pair<String, String>?>(null) } // type, msg
    var searchText by remember { mutableStateOf("") }

    // Quick Input State
    var manualDni by remember { mutableStateOf("") }

    // Search/Correction State
    var editingUser by remember { mutableStateOf<User?>(null) }
    var editDniVal by remember { mutableStateOf("") }

    // Logic: Process Entry
    fun processEntry(user: User, method: String) {
        val isReentry = scans.any { it.dni == user.dni }
        val type = if (isReentry) "Re-ingreso" else method

        // Add Record
        onAddScan(ScanRecord(
            Random.nextLong(),
            user.name,
            user.dni,
            "10:${Random.nextInt(10, 59)} AM",
            type
        ))

        // Feedback
        feedback = if (isReentry) "reentry" to "RE-INGRESO AUTORIZADO" else "success" to "INGRESO EXITOSO"

        // Reset Logic
        searchText = ""
        manualDni = ""
        editingUser = null
    }

    // Logic: Simulate Camera Scan
    fun simulateScan() {
        if (db.isNotEmpty()) {
            val randomUser = db.random()
            processEntry(randomUser, "QR")
        } else {
            // Mock if empty
            processEntry(User(0, "Demo User", "999999", "", "", ""), "QR")
        }
    }

    // Logic: Manual Submit
    fun submitManual() {
        val user = db.find { it.dni == manualDni }
        if (user != null) {
            processEntry(user, "Manual")
        } else {
            // Error logic could go here
        }
    }

    // Logic: Update User DNI
    fun saveUserDni() {
        editingUser?.let { user ->
            val index = db.indexOfFirst { it.id == user.id }
            if (index != -1) {
                val newUser = user.copy(dni = editDniVal)
                db[index] = newUser
                processEntry(newUser, "Manual")
            }
        }
    }

    // Auto-hide feedback
    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(2500)
            feedback = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(EduTheme.DarkHeader)) {

        // --- FEEDBACK OVERLAY ---
        if (feedback != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.8f))
                    .zIndex(100f)
                    .clickable { feedback = null },
                contentAlignment = Alignment.Center
            ) {
                val (type, msg) = feedback!!
                val color = if (type == "reentry") EduTheme.Warning else EduTheme.Success
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(4.dp, color, RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (type == "reentry") Icons.Default.Replay else Icons.Default.CheckCircle,
                        null,
                        tint = color,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(msg, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if(type=="reentry") EduTheme.WarningText else Color(0xFF1B5E20), textAlign = TextAlign.Center)
                    if (type == "reentry") {
                        Text("Usuario ya registrado previamente", fontSize = 12.sp, color = EduTheme.WarningText)
                    }
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
                    Box(modifier = Modifier.size(32.dp).background(EduTheme.BrandRed, CircleShape), contentAlignment = Alignment.Center) {
                        Text("ST", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("OPERADOR", color = Color.Gray, fontSize = 10.sp)
                        Text("EduTec Staff", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(Color(0xFF1B5E20).copy(0.3f), CircleShape).border(1.dp, Color(0xFF2E7D32), CircleShape).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("En línea", color = Color(0xFF4CAF50), fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onBack, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                        Text("Salir", fontSize = 10.sp)
                    }
                }
            }

            // CONTENT AREA
            Box(modifier = Modifier.weight(1f)) {
                if (mode == "camera") {
                    // === MODO CÁMARA REAL (NUEVO) ===
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {

                        // 1. LA CÁMARA (El ojo del robot)
                        CameraPreview(
                            reductionFactor = 1f,
                            onCameraStatusChanged = { active, denied ->
                                // Aquí puedes manejar permisos si quieres
                            },
                            onQrDetected = { qrCode ->
                                // Lógica: Buscar usuario en la base de datos local
                                val userFound = db.find { it.dni == qrCode }

                                if (userFound != null) {
                                    // SI EXISTE: Registramos entrada
                                    // IMPORTANTE: Asegúrate de tener la función 'processEntry' creada.
                                    // Si te marca error en rojo, usa 'simulateScan()' por mientras.
                                    processEntry(userFound, "QR")
                                } else {
                                    // NO EXISTE: Registramos error o desconocido
                                    processEntry(User(0, "Desconocido", qrCode, "", "Sin Registro", ""), "QR-Error")
                                }
                            }
                        )

                        // 2. EL MARCO (Reciclado de tu diseño anterior para que se vea bien)
                        Box(modifier = Modifier.size(280.dp).border(2.dp, Color.White.copy(0.3f), RoundedCornerShape(24.dp))) {
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(EduTheme.BrandRed).align(Alignment.Center))
                        }

                        // 3. TEXTO DE AYUDA
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Apunta al código QR",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                    // === AQUÍ TERMINA EL MODO CÁMARA ===

                } else {
                    // MANUAL MODE
                    Column(modifier = Modifier.fillMaxSize().background(EduTheme.DarkHeader).padding(16.dp)) {
                        // TABS
                        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)).padding(4.dp)) {
                            val activeColor = Color(0xFF1976D2)
                            Button(
                                onClick = { manualTab = "quick" },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if(manualTab=="quick") activeColor else Color.Transparent)
                            ) { Text("Ingreso Rápido", fontSize = 12.sp) }
                            Button(
                                onClick = { manualTab = "search" },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if(manualTab=="search") activeColor else Color.Transparent)
                            ) { Text("Corregir / Buscar", fontSize = 12.sp) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (manualTab == "quick") {
                            // Quick Tab
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF232323))) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Bolt, null, tint = Color(0xFF1976D2), modifier = Modifier.size(48.dp))
                                    Text("Ingreso Directo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Validar entrada por DNI", color = Color.Gray, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    OutlinedTextField(
                                        value = manualDni,
                                        onValueChange = { manualDni = it },
                                        placeholder = { Text("Escribe DNI...", color = Color.Gray, fontSize = 24.sp) },
                                        textStyle = LocalTextStyle.current.copy(fontSize = 24.sp, textAlign = TextAlign.Center, color = Color.White),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF1976D2),
                                            unfocusedBorderColor = Color.Gray
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = { submitManual() }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) {
                                        Text("MARCAR ASISTENCIA", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Search Tab
                            Column {
                                OutlinedTextField(
                                    value = searchText,
                                    onValueChange = { searchText = it },
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                                    placeholder = { Text("Buscar nombre...", color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF232323),
                                        unfocusedContainerColor = Color(0xFF232323),
                                        focusedBorderColor = Color(0xFF1976D2),
                                        unfocusedBorderColor = Color.Gray,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    val results = if(searchText.length > 1) db.filter { it.name.contains(searchText, true) } else emptyList()
                                    items(results) { user ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        ) {
                                            if (editingUser?.id == user.id) {
                                                // Edit Mode
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold)
                                                        Text("EDITANDO", color = Color(0xFF1976D2), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    OutlinedTextField(
                                                        value = editDniVal,
                                                        onValueChange = { editDniVal = it },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                                    )
                                                    Row(modifier = Modifier.padding(top = 8.dp)) {
                                                        Button(onClick = { editingUser = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar") }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Button(onClick = { saveUserDni() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))) { Text("Guardar") }
                                                    }
                                                }
                                            } else {
                                                // View Mode
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold)
                                                        Text("DNI: ${user.dni}", color = Color.Gray, fontSize = 12.sp)
                                                    }
                                                    IconButton(onClick = { editingUser = user; editDniVal = user.dni }) {
                                                        Icon(Icons.Default.Edit, null, tint = Color.Gray)
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
                Button(
                    onClick = { mode = "camera" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if(mode=="camera") Color(0xFF333333) else Color.Transparent)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, null)
                        Text("Cámara", fontSize = 10.sp)
                    }
                }
                Button(
                    onClick = { mode = "manual" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if(mode=="manual") Color(0xFF1976D2).copy(0.3f) else Color.Transparent)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, tint = if(mode=="manual") Color(0xFF64B5F6) else Color.White)
                        Text("Manual", fontSize = 10.sp, color = if(mode=="manual") Color(0xFF64B5F6) else Color.White)
                    }
                }
            }

            // HISTORY SHEET
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(EduTheme.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Column {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp).width(40.dp).height(4.dp).background(Color.LightGray, CircleShape))
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Aforo: ${scans.size}", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                        Text("EN VIVO", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 10.sp, modifier = Modifier.background(Color(0xFFE8F5E9), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                    Divider(color = Color(0xFFEEEEEE))
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(scans) { scan ->
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .fillMaxWidth()
                                    .border(1.dp, if(scan.type=="Re-ingreso") EduTheme.Warning else Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                    .background(if(scan.type=="Re-ingreso") EduTheme.WarningBg else EduTheme.White, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp).background(
                                        if(scan.type=="Re-ingreso") EduTheme.Warning else if(scan.type=="QR") Color(0xFFE8F5E9) else Color(0xFFE3F2FD),
                                        CircleShape
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if(scan.type=="Re-ingreso") Icons.Default.Replay else if(scan.type=="QR") Icons.Default.QrCode else Icons.Default.Keyboard,
                                        null,
                                        tint = if(scan.type=="Re-ingreso") Color.White else if(scan.type=="QR") Color(0xFF2E7D32) else Color(0xFF1565C0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(scan.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("ID: ${scan.dni}", fontSize = 10.sp, color = Color.Gray)
                                        if(scan.type=="Re-ingreso") {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("RE-INGRESO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EduTheme.WarningText, modifier = Modifier.background(EduTheme.Warning, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                                        }
                                    }
                                }
                                Text(scan.time, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
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

        // Estado Global (DB y Registros)
        val db = remember { mutableStateListOf<User>().apply { addAll(INITIAL_DB) } }
        val scans = remember { mutableStateListOf<ScanRecord>() }

        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                "home" -> HomeScreen(onNavigate = { currentScreen = it })
                "student" -> StudentRegisterScreen(
                    onBack = { currentScreen = "home" },
                    onRegister = { db.add(it) }
                )
                "admin" -> AdminScreen(
                    onBack = { currentScreen = "home" },
                    db = db,
                    scans = scans,
                    onAddScan = { scans.add(0, it) }
                )
            }
        }
    }
}