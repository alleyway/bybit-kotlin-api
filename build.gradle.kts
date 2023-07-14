project.version = "0.4.5-SNAPSHOT"
project.description = "ByBit API for Java/Kotlin"
buildscript {
    repositories {
        google()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.0")
    }
}

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    kotlin("kapt") version "1.9.0"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    val ktorVersion = "2.3.2"
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Annotation processor that generates Java builders for data classes
    val ktBuilderVersion = "1.2.1"
    implementation("com.thinkinglogic.builder:kotlin-builder-annotation:$ktBuilderVersion")
    kapt("com.thinkinglogic.builder:kotlin-builder-processor:$ktBuilderVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.0")
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


