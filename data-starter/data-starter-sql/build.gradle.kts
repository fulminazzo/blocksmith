dependencies {
    api(libs.jooq)
    api(libs.hikaricp)

    testImplementation(libs.h2)
    testImplementation(libs.sqlite)
    testImplementation(libs.mysql)
    testImplementation(libs.mariadb)
    testImplementation(libs.postgresql)
}
