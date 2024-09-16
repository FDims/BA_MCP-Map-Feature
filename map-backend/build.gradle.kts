plugins {
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
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
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// PostgreSQL driver
	runtimeOnly("org.postgresql:postgresql")

	// Mapnik wrapper for Java (you may need to find or create a Kotlin-specific library)
	implementation("org.mapnik:mapnik-jni:0.1.0")

	// GeoTools for additional geospatial operations
	implementation("org.geotools:gt-main:26.0")
	implementation("org.geotools:gt-epsg-hsql:26.0")

	// Logging
	implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
