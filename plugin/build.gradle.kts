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

    implementation("commons-io:commons-io:2.11.0")
    implementation("com.android.tools.build:gradle:4.0.2")

    testImplementation("junit:junit:4.13.2")
}

tasks.withType<Jar> {
    into("/") {
        from("libs")
    }
}

mavenPublish {
    sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
}