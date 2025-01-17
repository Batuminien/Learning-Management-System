package com.example.loginmultiplatform.ui.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.*
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loginmultiplatform.getPlatformResourceContainer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.loginmultiplatform.R
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.example.loginmultiplatform.viewmodel.ProfilePhotoViewModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.net.HttpURLConnection
import java.net.URL

val customFontFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_semibold, FontWeight.Bold)
)

@Composable
actual fun TopBar(
    userName: String?,
    userRole: String?,
    userId: Int,
    viewModel: ProfilePhotoViewModel,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: NavController
) {

    var expanded by remember { mutableStateOf(false) }
    val profilePhoto by viewModel.profilePhoto.observeAsState()

    LaunchedEffect(userId) {
        if(userId != -1) {
            viewModel.fetchProfilePhoto(userId)
        }
    }

    Row(
        modifier = Modifier
            .background(color = Color(0xFF334BBE))
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box {
            IconButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = { expanded = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            /*if (userRole == "ROLE_ADMIN") {
                IconButton(
                    modifier = Modifier.padding(start = 50.dp),
                    onClick = { }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = "Add user",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }*/

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSettingsClick()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Setings",
                            tint = Color(0xFF334BBE),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ayarlar", fontFamily = customFontFamily, fontWeight = FontWeight.Normal)
                    }
                }

                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        navController.navigate("login_screen") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Çıkış Yap", fontFamily = customFontFamily, fontWeight = FontWeight.Normal, color = Color.Red)
                    }
                }
            }
        }

        Image(
            painter = painterResource(id = getPlatformResourceContainer().appLogo),
            modifier = Modifier.size(150.dp).padding(top = 5.dp).offset(x = 10.dp),
            contentDescription = "App Logo"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = userName ?: "",
                fontSize = 14.sp,
                color = Color.White,
                fontFamily = customFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )

            IconButton(
                onClick = { onProfileClick() }
            ) {
                if (profilePhoto != null) {
                    Image(
                        bitmap = profilePhoto!!.asImageBitmap(),
                        contentDescription = "Profile Photo",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Default Profile Picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334BBE)),
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun BottomNavigationBar(pagerState: PagerState?, navController: NavController) {
    val items = listOf(
        Triple("Duyurular", Icons.Rounded.Campaign, "announcements_screen"),
        Triple("Yoklama", Icons.Rounded.EventNote, "attendance_screen"),
        Triple("Ana Menü", Icons.Rounded.Home, "dashboard_page"),
        Triple("Ödev", Icons.Rounded.AutoStories, "homework_screen"),
        Triple("Sınavlar", Icons.Rounded.LibraryBooks, "past_exams_screen")
    )
    //val coroutineScope = rememberCoroutineScope()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF334BBE))
            .padding(vertical = 12.dp),
            //.shadow(elevation = 8.dp, shape = MaterialTheme.shapes.medium),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        items.forEach { item ->
            val isSelected = currentRoute == item.third
            val alpha = if (isSelected) 1f else 0.5f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    navController.navigate(item.third) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {

                Icon(
                    imageVector = item.second,
                    contentDescription = item.first,
                    //tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(30.dp),
                    tint = Color.White.copy(alpha = alpha)
                )

                // Metin
                Text(
                    text = item.first,
                    color = Color.White.copy(alpha = alpha),
                    style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 4.dp),
                    fontFamily = customFontFamily
                )

                // Aktif sayfa göstergesi
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .height(2.dp)
                            .width(16.dp)
                            .background(Color.White, shape = MaterialTheme.shapes.small)
                    )
                }
            }
        }
    }
}

