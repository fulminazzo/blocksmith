@file:Suppress("UnstableApiUsage")

dependencies {
    api(libs.jooq)
    api(libs.hikaricp)
    
    integrationTestImplementation(libs.h2)
    integrationTestImplementation(libs.sqlite)

    integrationTestImplementation(libs.mysql)
    integrationTestImplementation(libs.test.containers.mysql)

    integrationTestImplementation(libs.mariadb)
    integrationTestImplementation(libs.test.containers.mariadb)

    integrationTestImplementation(libs.postgresql)
    integrationTestImplementation(libs.test.containers.postgresql)

    integrationTestImplementation(libs.yugabytedb)
    integrationTestImplementation(libs.test.containers.yugabytedb)
}
