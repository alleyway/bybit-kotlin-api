plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")

    // For custom interceptors on defaultOkHttpClientProvider
    implementation("com.squareup.okhttp3:okhttp:4.7.2")

    // For custom CIO engine on defaultJvmHttpClientProvider
    implementation("io.ktor:ktor-client-cio-jvm:2.3.1")

    // For pretty printing data classes
    implementation("com.tylerthrailkill.helpers:pretty-print:2.0.2")
}

task(name = "javaSample", type = JavaExec::class) {
    group = "samples"
    description = "Sample usage of the bybit client from Java"
//    mainClass = "bybit.sdk.sample.JavaUsageSample"
    classpath = sourceSets["main"].runtimeClasspath
}

task(name = "kotlinSample", type = JavaExec::class) {
    group = "samples"
    description = "Sample usage of the bybit client from Kotlin"
//    main = "bybit.sdk.sample.KotlinUsageSampleKt"
    classpath = sourceSets["main"].runtimeClasspath
}
