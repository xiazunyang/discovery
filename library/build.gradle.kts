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
    testImplementation(gradleApi())
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("java"))
                groupId = "cn.numeron"
                artifactId = "discovery.library"
                version = "1.0.0"
            }
        }
    }
}
