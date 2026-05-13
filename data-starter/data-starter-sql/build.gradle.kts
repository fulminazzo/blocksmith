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
                implementation(libs.test.containers.mysql)

                implementation(libs.mariadb)
                implementation(libs.test.containers.mariadb)

                implementation(libs.postgresql)
                implementation(libs.test.containers.postgresql)

            }
        }
    }
}