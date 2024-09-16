plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "de.cherry-tea.map-server"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	// Add OSGeo repository for GeoTools
	maven {
		url = uri("https://repo.osgeo.org/repository/release/")
	}
	// Add Boundless repository as a fallback
	maven {
		url = uri("https://repo.boundlessgeo.com/main/")
	}

	maven {
		url = uri("https://mvnrepository.com/artifact/javax.media/jai_core")
	}
}

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")

	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// Database
	implementation("org.postgresql:postgresql")

	// GIS libraries
	implementation("org.locationtech.jts:jts-core:1.18.2")
	implementation("org.geotools:gt-main:27.0")
	implementation("org.geotools:gt-epsg-hsql:27.0")
	implementation("org.geotools:gt-render:27.0")

	// Logging
	implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("javax.media:jai_core:1.1.3")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
