package it.fulminazzo.blocksmith.config

import spock.lang.Specification

class BeanConfigurationBuilderFunctionalTest extends Specification {

    def 'test generate of existing class with version'() {
        given:
        def configurationFile = new File('build/resources/functionalTest/config-version.yml')
        def sourceDirectory = new File('build/resources/functionalTest')
        def packageName = 'it.fulminazzo.blocksmith'
        def className = 'BlocksmithEnhancedConfigVersion'
        def expected = new File('src/functionalTest/resources/it/fulminazzo/blocksmith/BlocksmithEnhancedConfigVersion.java')

        when:
        def file = BeanConfigurationBuilder.generate(configurationFile, sourceDirectory, packageName, className)

        then:
        file.exists()

        and:
        file.readLines() == expected.readLines()
    }

    def 'test generate of not-existing class with version'() {
        given:
        def configurationFile = new File('build/resources/functionalTest/config-version.yml')
        def sourceDirectory = new File('build/resources/src')
        def packageName = 'it.fulminazzo.blocksmith'
        def className = 'BlocksmithConfigVersion'
        def expected = new File('build/resources/functionalTest/BlocksmithConfigVersion.java')

        and:
        if (sourceDirectory.exists()) sourceDirectory.deleteDir()

        when:
        def file = BeanConfigurationBuilder.generate(configurationFile, sourceDirectory, packageName, className)

        then:
        file.exists()

        and:
        file.readLines() == expected.readLines()
    }

    def 'test generate of existing class'() {
        given:
        def configurationFile = new File('build/resources/functionalTest/config.yml')
        def sourceDirectory = new File('build/resources/functionalTest')
        def packageName = 'it.fulminazzo.blocksmith'
        def className = 'BlocksmithEnhancedConfig'
        def expected = new File('src/functionalTest/resources/it/fulminazzo/blocksmith/BlocksmithEnhancedConfig.java')

        when:
        def file = BeanConfigurationBuilder.generate(configurationFile, sourceDirectory, packageName, className)

        then:
        file.exists()

        and:
        file.readLines() == expected.readLines()
    }

    def 'test generate of not-existing class'() {
        given:
        def configurationFile = new File('build/resources/functionalTest/config.yml')
        def sourceDirectory = new File('build/resources/src')
        def packageName = 'it.fulminazzo.blocksmith'
        def className = 'BlocksmithConfig'
        def expected = new File('build/resources/functionalTest/BlocksmithConfig.java')

        and:
        if (sourceDirectory.exists()) sourceDirectory.deleteDir()

        when:
        def file = BeanConfigurationBuilder.generate(configurationFile, sourceDirectory, packageName, className)

        then:
        file.exists()

        and:
        file.readLines() == expected.readLines()
    }

}
