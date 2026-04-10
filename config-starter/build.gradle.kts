val projectName: String = project.name
val coreModuleName = "core"
val testingModuleName: String by rootProject.extra

dependencies {
    compileOnly(libs.slf4j)
}

allprojects {

    dependencies {
        if (!project.name.endsWith(testingModuleName))
            testImplementation(project(":$projectName:$projectName-$testingModuleName"))
    }

}

subprojects {

    dependencies {
        compileOnly(rootProject.libs.slf4j)
        if (!project.name.endsWith(coreModuleName))
            api(project(":$projectName:$projectName-$coreModuleName"))
    }

}
