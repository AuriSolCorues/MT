import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// 签名凭据从 keystore.properties 读取（已 gitignore，永不入库；CI 由 secret 还原）。
// 空则 release 保持未签名——无条件挂 signingConfig 会以
// SigningConfig "release" is missing required property "storeFile" 失败，
// 而 mtci job 正是无 keystore 场景。
// 注：改此文件后需 ./gradlew --stop，configuration-cache 不跟踪它的内容。
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// 版本号：发布 job 经 -Pmt.versionName/-Pmt.versionCode 注入，本地不传时用默认值。
val appVersionName = providers.gradleProperty("mt.versionName").getOrElse("1.0")
val appVersionCode = providers.gradleProperty("mt.versionCode").getOrElse("1").toInt()

android {
    namespace = "com.copy.mt"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.copy.mt"
        minSdk = 24
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}