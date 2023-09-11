project.version = "0.4.7-SNAPSHOT"
project.description = "ByBit API for Java/Kotlin"
buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.10")
    }
}

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    kotlin("kapt") version "1.9.10"
    kotlin("plugin.lombok") version "1.9.10"
    id("io.freefair.lombok") version "8.1.0"
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

    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")

    val ktorVersion = "2.3.4"
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

//    val resilience4jVersion = "2.0.0"
//    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")
//    implementation("io.github.resilience4j:resilience4j-ratelimiter:$resilience4jVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")
    testImplementation(kotlin("test-junit5"))
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://jcenter.bintray.com") }
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

java {
    withJavadocJar()
    withSourcesJar()
}

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
            jvmToolchain(17)
        }
    }

    compileJava {
        targetCompatibility = JavaVersion.VERSION_17.toString()
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


tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask>()
    .configureEach {
        listOf("util", "file", "main", "jvm", "processing", "comp", "tree", "api", "parser", "code")
            .flatMap { listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.$it=ALL-UNNAMED") }
            .forEach(kaptProcessJvmArgs::addAll)
    }

//tasks.register("jvmReleaseSourcesJar", Jar::class) {
//    archiveClassifier.set("sources")
//    from(kotlin.sourceSets["main"].kotlin.srcDirs)
//}

//tasks.register<Jar>("javadocJar") {
//    archiveClassifier.set("javadoc")
//    from(tasks.named("javadoc"))
//}


val isSnapshot = project.version.toString().contains("SNAPSHOT")


if (!isSnapshot) {
    signing {
        useGpgCmd()
        sign(publishing.publications)

    }
}

publishing {
    publications {

        create<MavenPublication>("maven") {
            groupId = "io.github.alleyway"
            artifactId = "bybit-kotlin-api"
            version = project.version.toString()
            //artifact(sourcesJar)
            from(components["java"])
//            artifact(tasks.getByName("jvmReleaseSourcesJar"))
//            artifact(tasks["javadocJar"])

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
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/mit")
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

val signingTasks = tasks.withType<Sign>()

if (!isSnapshot) {
    tasks.withType<AbstractPublishToMaven>().configureEach { dependsOn(signingTasks) }
}


