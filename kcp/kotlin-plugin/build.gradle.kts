import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("kotlin-kapt")
    id("com.vanniktech.maven.publish")
    id("com.github.gmazzo.buildconfig")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.30")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    compileOnly("com.google.auto.service:auto-service:1.0")
    kapt("com.google.auto.service:auto-service:1.0")
    testImplementation("junit:junit:4.13.2")
}

mavenPublish {
    sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
}

buildConfig {
    packageName("cn.numeron.discovery.kotlin")
    buildConfigField("String", "ARTIFACT_ID", "\"${property("POM_ARTIFACT_ID")}\"")
    //buildConfigField("String", "VERSION", "\"${property("VERSION_NAME")}\"")
    //buildConfigField("String", "GROUP_ID", "\"${property("GROUP")}\"")
}