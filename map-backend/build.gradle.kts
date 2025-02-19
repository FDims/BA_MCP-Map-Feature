plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.3"
	id("io.spring.dependency-management") version "1.1.6"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "de.maptile"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral {
		content {
			excludeGroup("javax.media")
		}
	}
	maven {
		url = uri("https://repo.osgeo.org/repository/release/")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	implementation("org.geotools:gt-main:28.0")
	implementation("org.geotools:gt-epsg-hsql:28.0")
	implementation("org.geotools:gt-render:28.0")
	implementation("org.geotools:gt-jdbc:28.0")
	implementation("org.geotools.jdbc:gt-jdbc-postgis:28.0")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
