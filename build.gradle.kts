import org.jetbrains.kotlin.gradle.tasks.KaptGenerateStubs

buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.21")
    }
}

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    kotlin("kapt") version "1.8.21"
}

tasks.withType<KaptGenerateStubs> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {

	implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")

    val ktorVersion = "2.3.1"
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Annotation processor that generates Java builders for data classes
    val ktBuilderVersion = "1.2.2"
    implementation("com.thinkinglogic.builder:kotlin-builder-annotation:$ktBuilderVersion")
    kapt("com.thinkinglogic.builder:kotlin-builder-processor:$ktBuilderVersion")

	testImplementation(kotlin("test"))
	testImplementation(kotlin("test-junit5"))
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

//val sourcesJar = tasks.create("sources", Jar::class) {
//    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
//    classifier = "sources"
//    from(sourceSets["main"].allSource)
//}


tasks {

	test {
		useJUnitPlatform()
	}

    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    artifacts {
//        add("archives", sourcesJar)
        add("archives", jar)
    }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.alleyway"
            artifactId = "bybit-kotlin-api"
//            artifact(sourcesJar)

            from(components["java"])
        }
    }
}
