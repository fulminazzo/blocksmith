dependencies {
    api(projects.schedulerStarter.schedulerStarterBukkit)

    testImplementation(libs.mockbukkit20) {
        exclude(group = "io.papermc.paper")
        exclude(group = "net.bytebuddy")
        exclude(group = "org.junit.jupiter")
    }
}
