plugins { id("blocksmith.composite-module") }

dependencies {
    compileOnly(libs.slf4j)
}

allprojects {

    dependencies {
        compileOnly(rootProject.libs.slf4j)
    }

}

compositeModule {
    excludedSubmodules = setOf(
        project.projects.configStarter.configStarterGenerator.name
    )
}
