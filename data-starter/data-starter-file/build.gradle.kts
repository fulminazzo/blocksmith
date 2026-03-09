dependencies {
    compileOnly(libs.slf4j)

    api(project(":config-starter"))

    testImplementation(libs.slf4j)
    testImplementation(project(":config-starter:config-starter-json"))
}
