plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
    id("signing")
}

group = "cn.numeron"
version = "2.0.0"

java {
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(11)
}

signing {
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "discovery.library"
            version = project.version.toString()
            afterEvaluate {
                from(components["java"])
            }
            pom {
                name = "discovery"
                description = "AGP discovery implemented library."
                url = "https://github.com/xiazunyang/discovery"
                inceptionYear = "2023 last month"
                scm {
                    url = "https://github.com/xiazunyang/discovery"
                    connection = "scm:git:git://github.com/xiazunyang/discovery.git"
                    developerConnection = "scm:git:ssh://git@github.com:xiazunyang/discovery.git"
                }
                developers {
                    developer {
                        name = "xiazunyang"
                        email = "x.z.y.seido@outlook.com"
                        url = "https://github.com/xiazunyang"
                    }
                }
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
            }
        }
    }
    repositories {
        maven {
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = property("mavenCentralUsername") as String
                password = property("mavenCentralPassword") as String
            }
        }
    }
}
