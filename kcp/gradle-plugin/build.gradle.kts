import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("kotlin-kapt")
    id("java-gradle-plugin")
    id("com.vanniktech.maven.publish")
    id("com.github.gmazzo.buildconfig")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    compileOnly("com.google.auto.service:auto-service:1.0")
    kapt("com.google.auto.service:auto-service:1.0")
    testImplementation("junit:junit:4.13.2")
}

mavenPublish {
    sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
}

buildConfig {
    val project = project(":kcp:kotlin-plugin")
    packageName("cn.numeron.discovery.gradle")
    buildConfigField("String", "ARTIFACT_ID", "\"${project.property("POM_ARTIFACT_ID")}\"")
    buildConfigField("String", "VERSION", "\"${project.property("VERSION_NAME")}\"")
    buildConfigField("String", "GROUP_ID", "\"${project.property("GROUP")}\"")
}

gradlePlugin {
    plugins {
        create("DiscoveryGradlePlugin") {
            id = project(":kcp:kotlin-plugin").property("POM_ARTIFACT_ID") as String
            implementationClass = "cn.numeron.discovery.gradle.DiscoveryKotlinCompilerPlugin"
        }
    }
}