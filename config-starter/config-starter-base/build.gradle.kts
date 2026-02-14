dependencies {
    api(libs.joor)

    api(libs.jackson.json)

    api(project(":base:validation"))
    api(project.parent!!)
}