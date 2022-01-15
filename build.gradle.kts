plugins {
    `java-library`
    `maven-publish`
}

group = "edu.kit.satviz"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val devcordUsername: String by project
val devcordPassword: String by project

publishing {
    repositories {
        maven {
            val repo = "https://repo.devcord.club"
            val releaseRepo = "$repo/releases"
            val snapshotRepo = "$repo/snapshots"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotRepo else releaseRepo)

            authentication {
                credentials {
                    username = devcordUsername
                    password = devcordPassword
                }
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
