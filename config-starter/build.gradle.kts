dependencies {
    compileOnly(libs.slf4j)

    compileOnly(libs.jackson.json)

    api(libs.bundles.validation)

    subprojects.forEach {
        dependencies {
            api(project(":${project.name}"))
        }
    }

    testImplementation(libs.slf4j)
    testImplementation(libs.bundles.log4j)

    testImplementation(libs.jackson.json)
}
