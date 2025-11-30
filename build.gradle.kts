plugins {
    kotlin("jvm") version "2.2.20" apply false
    id("io.ktor.plugin") version "3.2.3" apply false
    id("org.springframework.boot") version "3.5.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "by.yayauheny"
    version = "0.1.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    // Common configuration for all subprojects
}
