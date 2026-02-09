plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.jooq)
}

dependencies {
    // Source: https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.7")
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
                    url = "jdbc:h2:./build/h2db/test;DATABASE_TO_LOWER=TRUE;INIT=RUNSCRIPT FROM 'src/main/resources/schema.sql';AUTO_SERVER=true"
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
