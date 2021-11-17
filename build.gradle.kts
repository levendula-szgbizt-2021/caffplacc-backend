plugins {
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.sonarqube") version "3.3"
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
    implementation ("io.springfox:springfox-boot-starter:3.0.0")
    implementation ("io.jsonwebtoken:jjwt:0.9.1")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
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

// set up sonarqube
sonarqube {
    properties {
        property("sonar.projectkey", "caffplacc-backend")
    }
}