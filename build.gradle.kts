import org.jetbrains.kotlin.konan.target.HostManager
plugins {
    kotlin("multiplatform") version "1.9.0"
    //id("org.springframework.boot") version "3.2.0"
    //id("io.spring.dependency-management") version "1.1.3"
    id("org.jetbrains.compose") version "1.5.0"
    id("com.android.application") version "8.2.2"
    //id("dev.icerock.mobile.multiplatform-resources") version "0.23.0"
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // JetBrains Compose repository(si
}

kotlin {
    androidTarget()

    if(HostManager.hostIsMac) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    } else {
        println("Skipping iOS targets on non-macOS host")
    }
    

    sourceSets {
        val commonMain by getting {
            //kotlin.srcDir("build/generated/moko/commonMain/src")
            dependencies {
                implementation(kotlin("stdlib"))
                // AspectJ
                //implementation("org.aspectj:aspectjrt:1.9.20")
                //implementation("org.aspectj:aspectjweaver:1.9.20")

                // SLF4J
                //implementation("org.slf4j:slf4j-api:2.0.9")
                implementation("androidx.compose.foundation:foundation:1.5.1")

                //implementation("org.slf4j:slf4j-simple:2.0.9")
                // Spring Core (Temel bileşenler için gerekli)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("androidx.navigation:navigation-compose:2.7.3")
                // Kotlin Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

                implementation("org.jetbrains.compose.runtime:runtime:1.5.0")
                implementation("org.jetbrains.compose.foundation:foundation:1.5.0")
                implementation("org.jetbrains.compose.material:material:1.5.0")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                // Retrofit
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation("com.squareup.retrofit2:converter-gson:2.9.0") // JSON dönüşümü için

                // OkHttp
                implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
                implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.3") // Loglama için
                //implementation("dev.icerock.moko:resources:0.23.0")



                // DatePicker (Melikşah)
                implementation("androidx.activity:activity-compose:1.7.2")
                implementation(platform("androidx.compose:compose-bom:2023.08.00"))
                implementation("androidx.compose.ui:ui")
                implementation("androidx.compose.ui:ui-graphics")
                implementation("androidx.compose.ui:ui-tooling-preview")
                implementation("androidx.compose.material3:material3")

                implementation("com.mohamedrejeb.calf:calf-file-picker:0.5.3")
                implementation("com.mohamedrejeb.calf:calf-file-picker-coil:0.5.1")
                implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha08")

                // File picker (Melikşah)
                implementation("com.darkrockstudios:mpfilepicker:3.1.0")
                implementation("com.google.accompanist:accompanist-permissions:0.33.1-alpha")

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            //kotlin.srcDir("build/generated/moko/androidMain/src")

            dependencies {
                // AspectJ
                /*implementation("org.aspectj:aspectjrt:1.9.20")
                implementation("org.aspectj:aspectjweaver:1.9.20")*/
                //implementation("androidx.compose.material3:material3-android:1.3.1")
                implementation("androidx.compose.foundation:foundation:1.5.1")
                // SLF4J
                //implementation("org.slf4j:slf4j-api:2.0.9")
                implementation("com.google.accompanist:accompanist-pager:0.30.0")
                implementation("com.google.accompanist:accompanist-pager-indicators:0.30.0")

                implementation("androidx.compose.material:material-icons-extended:1.5.4")
                //implementation("org.slf4j:slf4j-simple:2.0.9")
                implementation("androidx.compose.animation:animation:1.5.4")

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("androidx.navigation:navigation-compose:2.7.3")
                implementation("androidx.core:core-ktx:1.10.1")
                implementation("androidx.compose.ui:ui:1.5.0")
                implementation("androidx.compose.material:material:1.5.0")
                implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
                implementation("androidx.activity:activity-compose:1.7.2")
                implementation("com.google.android.material:material:1.9.0")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation("com.squareup.retrofit2:converter-gson:2.9.0") // JSON dönüşümleri için
                implementation("com.squareup.okhttp3:okhttp:4.10.0") // OkHttp
                implementation("com.squareup.okhttp3:logging-interceptor:4.10.0") // Loglama için
                /*implementation("dev.icerock.moko:resources-compose:0.23.0")
                implementation("dev.icerock.moko:resources:0.23.0")*/
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                //kotlin.srcDirs("build/generated/moko/androidMain/src")


                // DatePicker (Melikşah)
                implementation("androidx.activity:activity-compose:1.7.2")
                implementation(platform("androidx.compose:compose-bom:2023.08.00"))
                implementation("androidx.compose.ui:ui")
                implementation("androidx.compose.ui:ui-graphics")
                implementation("androidx.compose.ui:ui-tooling-preview")
                implementation("androidx.compose.material3:material3")

                implementation("com.mohamedrejeb.calf:calf-file-picker:0.5.3")
                implementation("com.mohamedrejeb.calf:calf-file-picker-coil:0.5.1")
                implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha08")
                implementation("io.coil-kt.coil3:coil:3.0.0-alpha08")
                implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-alpha08") // Sürüm uyumu




                // File Picker (Melikşah)
                implementation("com.darkrockstudios:mpfilepicker:3.1.0")
                implementation("com.google.accompanist:accompanist-permissions:0.33.1-alpha")

            }
        }
        
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation(kotlin("test-junit"))
                implementation("androidx.test.espresso:espresso-core:3.5.1")
            }
        }
        /*val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("dev.icerock.moko:resources:0.20.1")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }*/

        androidUnitTest.dependsOn(commonTest)
        //iosTest.dependsOn(commonTest)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}


android {
    namespace = "com.example.loginmultiplatform"
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        applicationId = "com.example.loginmultiplatform"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

/*multiplatformResources {
    multiplatformResourcesPackage = "com.example.loginmultiplatform" // Use your project(s package name here
    //useExperimentalPlugin = true
    //disableStaticFrameworkWarning = true
    //disable("iosX64", "iosArm64", "iosSimulatorArm64")
}*/
