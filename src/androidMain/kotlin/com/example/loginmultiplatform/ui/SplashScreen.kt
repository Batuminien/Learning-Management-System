package com.example.loginmultiplatform.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.loginmultiplatform.R
import kotlinx.coroutines.delay

@Composable
actual fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    // Logo animasyonu: aşağıdan yukarı
    val logoOffset by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 100.dp,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "logo_offset"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f, // Opaklık: 1f = tamamen görünür, 0f = tamamen şeffaf
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "logo_alpha"
    )

    // Animasyon başlatıcı
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000) // 2 saniye bekle
        navController.navigate("login_screen") // Login ekranına geçiş yap
    }

    // Gradient arkaplan
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5270FF), // İlk renk
                        Color(0xFF14267C)  // İkinci renk
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Logo animasyonu
        Image(
            painter = painterResource(id = R.drawable.app_logo), // Logonuzun kaynağı
            contentDescription = "Logo",
            modifier = Modifier
                .size(300.dp)
                .offset(y = logoOffset)
                .alpha(logoAlpha)
        )
    }
}