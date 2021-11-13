plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.springframework.boot") version "2.5.6"
    java
    `maven-publish`
}

group = "hu.bme.szgbizt.levendula"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}


// use JUnit5 for all test tasks
tasks.withType<Test> {
    useJUnitPlatform()
}

// configure publishing to maven
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

}
tasks.withType<GenerateMavenPom>().configureEach {
    destination = layout.projectDirectory.file("pom.xml").getAsFile()
}
tasks.register("pom") {
    dependsOn("generatePomFileForMavenPublication")
}
