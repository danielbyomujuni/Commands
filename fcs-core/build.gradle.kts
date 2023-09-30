plugins {
    java
    id ("io.freefair.lombok") version "8.3"
}

group = "dev.frydae"
version = "0.0.1"

apply (from = "$rootDir/common.gradle")
apply (from = "$rootDir/publishing.gradle")