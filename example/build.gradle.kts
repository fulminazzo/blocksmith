plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.jooq)
}

dependencies {
    implementation(project(":data-starter:data-starter-sql"))
    jooqGenerator(libs.h2)
    implementation(libs.h2)
    implementation(libs.bundles.log4j)
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:./build/resources/main/data/sql/test;" +
                            "DATABASE_TO_LOWER=TRUE;" +
                            "INIT=RUNSCRIPT FROM '${project.file("src/main/resources/schema.sql").absolutePath}';" +
                            "AUTO_SERVER=true"
                    user = "sa"
                    password = ""
                }

                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "public"
                        outputSchema = "test"
                    }

                    target.apply {
                        packageName = "it.fulminazzo.blocksmith.data.sql"
                        directory = "build/generated-sources/jooq"
                    }
                }
            }
        }
    }
}

tasks.compileJava {
    dependsOn("generateJooq")
}

dependencies {
    api(libs.embedded.redis)

    api(project(":data-starter:data-starter-redis"))
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.bundles.log4j)

    implementation(project(":config-starter:config-starter-all"))
    implementation(project(":data-starter:data-starter-file"))
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
