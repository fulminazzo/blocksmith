repositories {
    maven {
        name = "minecraft-libraries"
        url = uri("https://libraries.minecraft.net/")
    }
}

dependencies {
    compileOnly(libs.brigadier)

    testImplementation(libs.brigadier)
}
