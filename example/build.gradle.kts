plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.jooq)
}

tasks.compileJava {
    dependsOn("generateJooq")
}

dependencies {
    implementation(libs.bundles.log4j)

    implementation(project(":data-starter:data-starter-sql"))
    implementation(project(":data-starter:data-starter-redis"))
    implementation(project(":data-starter:data-starter-cache"))
    implementation(project(":data-starter:data-starter-memory"))

    jooqGenerator(libs.h2)
    implementation(libs.mariadb)
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
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
                        packageName = "it.fulminazzo.blocksmith.data.jooq"
                        directory = "build/generated-sources/jooq"
                    }
                }
            }
        }
    }
}
