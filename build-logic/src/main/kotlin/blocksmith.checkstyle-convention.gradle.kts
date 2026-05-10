/**
 * Plugin for applying uniform Checkstyle conventions across submodules.
 */

plugins {
    checkstyle
}

allprojects {
    apply { plugin("checkstyle") }

    checkstyle {
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        maxErrors = 0
        maxWarnings = 0
        toolVersion = rootProject.libs.versions.checkstyle.get()
    }

    tasks.withType<Checkstyle> {
        reports {
            xml.required = true
            html.required = true
        }
    }

}