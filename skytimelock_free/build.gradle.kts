plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Google 서비스 플러그인이 필요한 경우 주석 해제
    // alias(libs.plugins.google.services)
}

android {
    namespace = "com.skynet.skytimelock.free"
    compileSdk = 35 // 참조 파일에 맞춰 34로 수정

    defaultConfig {
        applicationId = "com.skynet.skytimelock.free"
        minSdk = 23 // 참조 파일에 맞춰 23으로 수정
        targetSdk = 35 // 참조 파일에 맞춰 34로 수정
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true // 참조 파일의 multidex 설정 추가
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        encoding = "UTF-8" // 로그캣 한글 깨짐 방지
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    // 필요한 경우 dexOptions 추가 (주석 처리된 부분)
    /*
    dexOptions {
        jumboMode = true
        javaMaxHeapSize = "4g"
    }
    */
}

dependencies {
    // 기존 의존성 유지
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // 테스트 의존성 유지
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 참조 파일에서 추가된 의존성
    implementation("com.android.support:multidex:1.0.3") // Multidex 지원
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.android.support:support-annotations:28.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.vectordrawable:vectordrawable:1.2.0")
    implementation("com.google.android.gms:play-services:12.0.1")
    implementation("com.google.android.gms:play-services-ads-lite")
    implementation("com.google.android.material:material:1.12.0")

    // AdMob 광고
    implementation("com.google.android.gms:play-services-ads:24.1.0") {
        exclude(group = "com.google.android.gms", module = "play-services-ads-lite")
    }

    // 기존 play-services-ads-api 유지
    implementation(libs.play.services.ads.api)
}