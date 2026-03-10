val projectName: String = project.name
val baseModuleName = "base"
val testingModuleName: String by rootProject.extra

dependencies {
    compileOnly(libs.slf4j)
}

allprojects {
    dependencies {
        testImplementation(rootProject.libs.slf4j)
        testImplementation(rootProject.libs.bundles.log4j)

        if (!project.name.endsWith(testingModuleName))
            testImplementation(project(":$projectName:$projectName-$testingModuleName"))
    }
}

subprojects {
    dependencies {
        compileOnly(rootProject.libs.slf4j)
        if (!project.name.endsWith(baseModuleName))
            api(project(":$projectName:$projectName-$baseModuleName"))
    }
}
