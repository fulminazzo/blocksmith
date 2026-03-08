dependencies {
    api(libs.validation)

    api(libs.jooq)
    api(libs.hikaricp)

    testImplementation(libs.h2)
    testImplementation(libs.sqlite)
}
