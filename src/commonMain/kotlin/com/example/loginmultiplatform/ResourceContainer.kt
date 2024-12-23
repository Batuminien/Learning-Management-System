// File: src/commonMain/kotlin/com/example/loginmultiplatform/ResourceContainer.kt

package com.example.loginmultiplatform

interface ResourceContainer {
    val lighthouse: Int
    val appLogo: Int
    val logo: Int
    val notification: Int
    val pp: Int
    val sidemenu: Int
    val mainpage: Int
    val exams: Int
    val homework: Int
    val attendance: Int
    val announcement: Int
    val eyeOpen: Int
    val eyeClose: Int
    val welcomeAgain: String
    val emailPlaceholder: String
    val passwordPlaceholder: String
    val rememberMe: String
    val forgotPassword: String
    val signIn: String
    val settings: Int
    val logo_sekil: Int
    val logo_yazi: Int
    val app_logo_login: Int
}

expect fun getPlatformResourceContainer(): ResourceContainer
