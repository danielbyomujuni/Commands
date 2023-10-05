plugins {
    java
    id ("io.freefair.lombok") version "8.3"
}

val jdaVersion = "5.0.0-beta.15"

group = "dev.frydae"
version = jdaVersion

apply (from = "$rootDir/common.gradle")
apply (from = "$rootDir/publishing.gradle")

repositories {
    maven {
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation ("org.junit.jupiter:junit-jupiter-params:5.8.1")
    testImplementation ("org.junit.platform:junit-platform-suite:1.9.1")

    implementation ("net.dv8tion:JDA:$jdaVersion") {
        exclude ("opus-java")
    }

    implementation (project(":fcs-core"))

    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.googlecode.json-simple:json-simple:1.1.1")
    implementation ("com.google.auto.service:auto-service-annotations:1.0.1")
    annotationProcessor ("com.google.auto.service:auto-service:1.0.1")


    implementation ("org.apache.commons:commons-lang3:3.1")
    implementation ("org.apache.commons:commons-csv:1.5")

    testImplementation ("org.mockito:mockito-core:5.5.0")

    runtimeOnly ("ch.qos.logback:logback-classic:1.2.8")
}