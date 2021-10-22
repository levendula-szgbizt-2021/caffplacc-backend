plugins {
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("org.springframework.boot") version "2.5.6"
	java
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

tasks.withType<Test> {
	useJUnitPlatform()
}
