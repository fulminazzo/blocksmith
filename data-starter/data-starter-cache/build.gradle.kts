dependencies {
    api(project(":data-starter:data-starter-memory"))

    testImplementation(project(":data-starter:data-starter-redis"))
    testImplementation(project(":data-starter:data-starter-sql"))

    testImplementation(libs.bundles.log4j)

    testImplementation(libs.embedded.redis)
    testImplementation(libs.h2)
}