@file:Suppress("UnstableApiUsage")

dependencies {
    api(libs.jooq)
    api(libs.hikaricp)
}

testing {
    suites {
        withType<JvmTestSuite> {
            dependencies {
                implementation(libs.h2)
                implementation(libs.sqlite)
                implementation(libs.mysql)
                implementation(libs.mariadb)
                implementation(libs.postgresql)
            }
        }
    }
}