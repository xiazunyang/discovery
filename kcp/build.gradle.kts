import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
    id("com.vanniktech.maven.publish")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(gradleApi())
    implementation(project(":core"))
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.5.21")
    testImplementation("junit:junit:4.13.2")
}

mavenPublish {
    sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
}