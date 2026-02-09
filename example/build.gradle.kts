plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.jooq)
}

dependencies {
    implementation(project(":data-starter:data-starter-sql"))
    jooqGenerator(libs.h2)
    implementation(libs.h2)
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:./build/h2db/test;INIT=RUNSCRIPT FROM 'src/main/resources/schema.sql'"
                    user = "sa"
                    password = ""
                }

                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "PUBLIC"
                    }

                    target.apply {
                        packageName = "it.fulminazzo.blocksmith.sql"
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

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier = "all"
}
