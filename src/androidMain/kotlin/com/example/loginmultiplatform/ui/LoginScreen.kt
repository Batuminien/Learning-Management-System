// File: src/commonMain/kotlin/com/example/loginmultiplatform/LoginScreen.kt

package com.example.loginmultiplatform.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.example.loginmultiplatform.R
import com.example.loginmultiplatform.getPlatformResourceContainer
import com.example.loginmultiplatform.ResourceContainer
import com.example.loginmultiplatform.viewmodel.LoginViewModel

@Composable
actual fun LoginScreen(viewModel: LoginViewModel, navController: NavController) {
    //val context = LocalContext.current
    val resources = getPlatformResourceContainer()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") } // Başarı mesajı için state
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") } // Hata mesajı için state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = resources.lighthouse),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // App Logo
            Icon(
                painter = painterResource(id = resources.appLogo),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(2.dp))

            // Email Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(text = resources.emailPlaceholder, color=Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF5270FF)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            var passwordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = resources.passwordPlaceholder, color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {

                    Row {
                        // X icon: Her zaman metin yazıldığında göster
                        if (password.isNotEmpty()) {
                            IconButton(onClick = { password = "" }, modifier = Modifier.alpha(1f)) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                    contentDescription = "Clear text",
                                    tint = Color.White
                                )
                            }
                        }

                        // Eye icon: Şifre görünürlüğünü kontrol eder
                        IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.alpha(1f)) {
                            val eyeIcon = if (passwordVisible) {
                                resources.eyeClose // Şifreyi gösterirken göz
                            } else {
                                resources.eyeOpen // Şifreyi gizlerken göz
                            }
                            Icon(
                                painter = painterResource(id = eyeIcon),
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF5270FF)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Remember Me and Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var rememberMe by remember { mutableStateOf(false) }
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            //checkedColor = Color.White, // Seçili olduğunda beyaz
                            uncheckedColor = Color.White, // Seçili değilken beyaz
                            //checkmarkColor = Color.Black // İşaret rengi siyah (isteğe bağlı)
                        )

                    )
                    Text(text = resources.rememberMe, color = Color.White)
                }
                TextButton(onClick = { /* Handle forgot password */ }) {
                    Text(text = resources.forgotPassword, color = Color(0xFF5270FF))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Sign In Button
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Email and Password cannot be empty"
                        showErrorDialog = true
                    } else {
                        viewModel.login(
                            username = username,
                            password = password,
                            onSuccess = { loginData ->
                                successMessage = "Login Successful!"
                                showSuccessDialog = true
                                when (loginData.role) {
                                    "ROLE_STUDENT" -> navController.navigate("student_dashboard")
                                    "ROLE_TEACHER" -> navController.navigate("teacher_dashboard")
                                    "ROLE_COORDINATOR" -> navController.navigate("coordinator_dashboard")
                                    "ROLE_ADMIN" -> navController.navigate("admin_dashboard")
                                }
                            },
                            onError = { error ->
                                errorMessage = "Error: $error"
                                showErrorDialog = true
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5270FF))
            ) {
                Text(
                    text = resources.signIn,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    title = { Text("Error") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        Button(onClick = { showErrorDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    title = { Text("Success") },
                    text = { Text(successMessage) },
                    confirmButton = {
                        Button(onClick = { showSuccessDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // Mock ResourceContainer for preview
    val mockResources = object : ResourceContainer {
        override val logo: Int = R.drawable.logo
        override val notification: Int = R.drawable.notification
        override val pp: Int = R.drawable.pp
        override val sidemenu: Int = R.drawable.sidemenu
        override val mainpage: Int = R.drawable.mainpage
        override val exams: Int = R.drawable.exams
        override val homework: Int = R.drawable.homework
        override val attendance: Int = R.drawable.attendance
        override val announcement: Int = R.drawable.announcement
        override val lighthouse: Int = android.R.drawable.ic_menu_gallery
        override val appLogo: Int = android.R.drawable.sym_def_app_icon
        override val eyeOpen: Int = android.R.drawable.ic_menu_view
        override val eyeClose: Int = android.R.drawable.ic_menu_view
        override val settings: Int = android.R.drawable.s
        override val welcomeAgain: String = "Welcome Again"
        override val emailPlaceholder: String = "Email"
        override val passwordPlaceholder: String = "Password"
        override val rememberMe: String = "Remember Me"
        override val forgotPassword: String = "Forgot Password?"
        override val signIn: String = "Sign In"
    }

    // Use the mock resources in the preview
    LoginScreenContent(resources = mockResources)
}

@Composable
fun LoginScreenContent(resources: ResourceContainer) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = resources.lighthouse),
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            alignment = Alignment.TopCenter
        )

        // Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // App Logo
            Icon(
                painter = painterResource(id = resources.appLogo),
                contentDescription = "App Logo",
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Welcome Text
            Text(
                text = resources.welcomeAgain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            var email by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = resources.emailPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            var password by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(text = resources.passwordPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = android.R.drawable.ic_menu_view)
                    else
                        painterResource(id = android.R.drawable.ic_menu_close_clear_cancel)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, contentDescription = null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Remember Me and Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var rememberMe by remember { mutableStateOf(false) }
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(text = resources.rememberMe, color = Color.Gray)
                }
                TextButton(onClick = { /* Handle forgot password */ }) {
                    Text(text = resources.forgotPassword, color = Color.Blue)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Sign In Button
            Button(
                onClick = { /* Handle sign-in logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text(
                    text = resources.signIn,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}*/
