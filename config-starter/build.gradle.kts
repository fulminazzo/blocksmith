val excludedSubmodules: MutableSet<String> by extra
excludedSubmodules.add(project.projects.configStarter.configStarterGenerator.name)

dependencies {
    compileOnly(libs.slf4j)
}

allprojects {

    dependencies {
        compileOnly(rootProject.libs.slf4j)
    }

}
