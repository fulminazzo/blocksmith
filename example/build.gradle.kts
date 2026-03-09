plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.jooq)
}

tasks.compileJava {
    dependsOn("generateJooq")
}

dependencies {
    implementation(libs.bundles.log4j)

    implementation(project(":config-starter:config-starter-yaml"))
    implementation(project(":config-starter:config-starter-data"))

    implementation(project(":data-starter:data-starter-all"))

    jooqGenerator(libs.h2)
    implementation(libs.mariadb)

    testImplementation(libs.embedded.mariadb)
    testImplementation(libs.embedded.redis)
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

tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}
