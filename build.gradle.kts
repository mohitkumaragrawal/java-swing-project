plugins {
    id("java")
}

group = "proj"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("mysql:mysql-connector-java:8.0.28")
}

tasks.register("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath.get())
    into("$buildDir/libs")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "proj.Main"
    }
}
