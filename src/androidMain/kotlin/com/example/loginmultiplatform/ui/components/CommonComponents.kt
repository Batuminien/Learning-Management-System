package com.example.loginmultiplatform.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.loginmultiplatform.R
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@Composable
actual fun TopBar(userName: String?, onSettingsClick: () -> Unit, onProfileClick: () -> Unit) {

    val customFontFamily = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold),
        Font(R.font.montserrat_semibold, FontWeight.Bold)
    )

    Row(
        modifier = Modifier
            .background(color = Color(0xFF334BBE))
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            modifier = Modifier.padding(start = 8.dp),
            onClick = onSettingsClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
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
                onClick = onProfileClick
            ) {
                Image(
                    painter = painterResource(id = getPlatformResourceContainer().pp),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentDescription = "Profile Picture"
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun BottomNavigationBar(pagerState: PagerState) {
    val items = listOf(
        Triple("Duyurular", Icons.Rounded.Campaign, 3),
        Triple("Yoklama", Icons.Rounded.EventNote, 0),
        Triple("Ana Menü", Icons.Rounded.Home, 1),
        Triple("Ödev", Icons.Rounded.AutoStories, 2)
    )
    val coroutineScope = rememberCoroutineScope()

    val customFontFamily = FontFamily(
        Font(R.font.montserrat_regular, FontWeight.Normal),
        Font(R.font.montserrat_bold, FontWeight.Bold),
        Font(R.font.montserrat_semibold, FontWeight.Bold)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFF334BBE))
            .padding(vertical = 12.dp),
            //.shadow(elevation = 8.dp, shape = MaterialTheme.shapes.medium),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        items.forEach { (title, iconRes, index) ->
            val isSelected = pagerState.currentPage == index
            val alpha = if (isSelected) 1f else 0.5f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            ) {

                Icon(
                    imageVector = iconRes,
                    contentDescription = title,
                    //tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(30.dp),
                    tint = Color.White.copy(alpha = alpha)
                )

                // Metin
                Text(
                    text = title,
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

