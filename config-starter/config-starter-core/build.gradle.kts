dependencies {
    api(libs.jackson.json)

    api(project(":base:validation"))

    api(project.parent!!)
}
