# ipasir4j

ipasir4j provides a complete set of bindings to the [ipasir C interface](https://github.com/biotomas/ipasir).
For every function in the C interface, ipasir4j provides a corresponding Java method, allowing you to 
access and control incremental SAT solvers from Java without any intermediary C code.

## Include it in your project

Gradle (Kotlin DSL):
```kotlin
repositories {
    maven("https://repo.devcord.club/releases")
}

dependencies {
    implementation("edu.kit:ipasir4j:0.1.0")
}
```

Maven:
```xml
<repositories>
    <repository>
        <id>devcord-repo</id>
        <url>https://repo.devcord.club/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>edu.kit</groupId>
        <artifactId>ipasir4j</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

## Usage
WIP...
