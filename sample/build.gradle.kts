plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    // For custom interceptors on defaultOkHttpClientProvider
    implementation("com.squareup.okhttp3:okhttp:4.7.2")

    // For custom CIO engine on defaultJvmHttpClientProvider
    implementation("io.ktor:ktor-client-cio-jvm:2.3.4")

    // For pretty printing data classes
    implementation("com.tylerthrailkill.helpers:pretty-print:2.0.2")
}

tasks {

    compileJava {
        targetCompatibility = JavaVersion.VERSION_17.toString()
        sourceCompatibility = JavaVersion.VERSION_17.toString()
    }

    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
}


task(name = "kotlinSample", type = JavaExec::class) {
    group = "samples"
    description = "Sample usage of the bybit client from Kotlin"
//    main = "bybit.sdk.sample.KotlinUsageSampleKt"
    classpath = sourceSets["main"].runtimeClasspath
}
