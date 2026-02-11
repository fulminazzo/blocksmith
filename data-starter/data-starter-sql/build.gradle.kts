dependencies {
    api(libs.jooq)
    api(libs.hikaricp)

    api(project(":data-starter:data-starter-base"))

    testImplementation(libs.h2)
    testImplementation(libs.sqlite)
}
