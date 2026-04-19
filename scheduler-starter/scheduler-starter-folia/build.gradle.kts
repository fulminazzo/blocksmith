dependencies {
    api(projects.schedulerStarter.schedulerStarterBukkit)

    compileOnly(libs.folia)

    testImplementation(libs.folia)
    testImplementation(libs.mockbukkit) {
        exclude(group = "io.papermc.paper")
        exclude(group = "net.bytebuddy")
        exclude(group = "org.junit.jupiter")
    }
}
