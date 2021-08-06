import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
    id("maven-publish")
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
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))

    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")

    implementation("commons-io:commons-io:2.11.0")

    implementation("com.android.tools.build:gradle:7.0.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
}

tasks.withType<Jar> {
    into("/") {
        from("libs")
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("java"))
                groupId = "cn.numeron"
                artifactId = "discovery.plugin"
                version = "1.0.0"
            }
        }
    }
}