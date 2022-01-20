plugins {
    `java-library`
    `maven-publish`
    checkstyle
}

group = "edu.kit"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {
    getByName<Test>("test") {
        useJUnitPlatform()
    }

    val javadoc = getByName<Javadoc>("javadoc")

    register<Jar>("javadocJar") {
        dependsOn.add(javadoc)
        from(javadoc.destinationDir)
        archiveClassifier.set("javadoc")
    }

    register<Jar>("sourcesJar") {
        dependsOn.add("classes")
        from(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].allSource)
        archiveClassifier.set("sources")
    }
}

checkstyle {
    configFile = configDirectory.file("google_checks.xml").get().asFile
    toolVersion = "9.2.1"
}

val javadocJar = tasks.getByName("javadocJar")
val sourcesJar = tasks.getByName("sourcesJar")

artifacts {
    archives(javadocJar)
    archives(sourcesJar)
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
            artifact(javadocJar)
            artifact(sourcesJar)

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}
