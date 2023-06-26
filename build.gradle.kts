import org.gradle.internal.impldep.org.apache.maven.model.License

project.version = "0.2-SNAPSHOT"
project.description = "ByBit API for Java/Kotlin"
buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.22")
    }
}

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.serialization") version "1.8.22"
    kotlin("kapt") version "1.8.22"
}

//tasks.withType<KaptGenerateStubs> {
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//}

//tasks.withType<JavaCompile> {
//    targetCompatibility = "1.8"
//}

dependencies {

    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")

    val ktorVersion = "2.3.1"
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Annotation processor that generates Java builders for data classes
    val ktBuilderVersion = "1.2.1"
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
//
//val sourcesJar = tasks.create("sources", Jar::class) {
//    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
////    classifier = "sources"
//    from(sourceSets["main"].allSource)
//}

//val javaJar = tasks.create("jar", Jar::class) {
//    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
////    classifier = "sources"
//    from(java["main"].allSource)
//}


tasks {

    publishing {
        processResources {
            exclude("bybit.properties")
        }

    }

    test {
        useJUnitPlatform()
    }

    kapt {
        kotlin {
            jvmToolchain(11)
        }
    }

    compileJava {
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }

    compileKotlin {

        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }



    artifacts {
//        add("archives", sourcesJar)
        add("archives", jar)
    }
}

tasks.register("androidReleaseSourcesJar", Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets["main"].kotlin.srcDirs)
}

val isSnapshot = project.version.toString().contains("SNAPSHOT")


publishing {
    publications {

        create<MavenPublication>("maven") {
            groupId = "io.github.alleyway"
            artifactId = "bybit-kotlin-api"
            version = project.version.toString()
            //artifact(sourcesJar)
            from(components["java"])
            artifact(tasks.getByName("androidReleaseSourcesJar"))

            pom {
                name.set(provider {
                    project.description ?: "${project.group}:${project.name}"
                })
                description.set(provider {
                    project.description
                })
                url.set("https://junit.org/junit5/")
                scm {
                    connection.set("scm:git:git://github.com/alleyway/bybit-kotlin-api.git")
                    developerConnection.set("scm:git:git://github.com/alleyway/bybit-kotlin-api.git")
                    url.set("https://github.com/alleyway/bybit-kotlin-api")
                }
                licenses {
                    license {
                        val license: License by rootProject.extra
                        name.set(license.name)
                        url.set(license.url.toString())
                    }
                }
                developers {
                    developer {
                        id.set("mlake900")
                        name.set("Michael L")
                        email.set("mlake@alleywayapps.com")
                    }
                }
            }

        }

    }

    repositories {

        maven {
            name = "sonatype"
            credentials(PasswordCredentials::class)

            val releaseRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

            url = uri(if (isSnapshot) snapshotRepo else releaseRepo)
        }
    }

}

