dependencies {
    api(libs.rabbitmq)

    integrationTestImplementation(libs.test.containers.rabbitmq)
}
