dependencies {
    api(project(":message-starter:message-starter-velocity"))

    compileOnly(libs.velocity)

    testImplementation(libs.velocity)
}
