plugins {
    id("java-library")
    id("groovy")
    id("jacoco-report-aggregation")
}

group = "it.fulminazzo"
version = "0.0.1-SNAPSHOT"

allprojects {
    apply { plugin("java-library") }
    apply { plugin("groovy") }
    apply { plugin("jacoco-report-aggregation") }

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly(libs.bundles.annotations)
        annotationProcessor(libs.lombok)

        testImplementation(libs.bundles.annotations)
        testImplementation(libs.bundles.test.framework)
    }
    
    tasks.test {
        useJUnitPlatform()
    }

}

dependencies {
    subprojects.forEach { implementation(project(it.path)) }
}

tasks.testCodeCoverageReport {
    dependsOn(tasks.test)
    reports { 
        xml.required = true
        csv.required = true
    }
}
