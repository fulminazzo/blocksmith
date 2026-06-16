plugins { id("blocksmith.composite-module") }

dependencies {
    integrationTestImplementation(libs.test.containers.rabbitmq)
    integrationTestImplementation(libs.test.containers.kafka)
}
