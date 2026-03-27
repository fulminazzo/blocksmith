dependencies {
    api(project(":message-starter:message-starter-velocity"))
    api(project(":command-starter:command-starter-brigadier"))

    compileOnly(libs.velocity)

    testImplementation(libs.velocity)
}
