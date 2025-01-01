package com.example.loginmultiplatform.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
expect fun TopBar(userName: String?, userId: Int, onSettingsClick: () -> Unit, onProfileClick: () -> Unit, navController: NavController)

@OptIn(ExperimentalFoundationApi::class)
@Composable
expect fun BottomNavigationBar(pagerState: PagerState?, navController: NavController)
