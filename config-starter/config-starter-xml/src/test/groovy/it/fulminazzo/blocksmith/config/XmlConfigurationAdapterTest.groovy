package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class XmlConfigurationAdapterTest extends MigrationConfigurationAdapterTest {

    def 'test that load correctly loads file'() {
        given:
        def file = getFile('load')

        and:
        def adapter = getAdapter()

        when:
        def actual = adapter.load(file, MockConfig)

        then:
        noExceptionThrown()

        and:
        actual == new MockConfig(
                false,
                'Blocksmith',
                null,
                ['Fulminazzo', 'Camilla', 'Alex'],
                new MockConfig.Internal(1.0, null)
        )
    }

    def 'test that store correctly saves file'() {
        given:
        def file = getFile('store')
        if (file.exists()) file.delete()

        and:
        def adapter = getAdapter()

        when:
        adapter.store(file, new MockConfig())

        then:
        noExceptionThrown()

        and:
        file.readLines() == [
                '<MockConfig>',
                '  <!-- Example comment -->',
                '  <CommentsEnabled>true</CommentsEnabled>',
                '  <!-- This comment should be -->',
                '  <!-- Multiline! -->',
                '  <Name>blocksmith</Name>',
                '  <Description>This is the description for the configuration file.',
                'Should be written in multiline format.</Description>',
                '  <Authors>',
                '    <Authors>Fulminazzo</Authors>',
                '    <Authors>Camilla</Authors>',
                '  </Authors>',
                '  <Internal>',
                '    <!-- This comment should be indented -->',
                '    <!-- And should be multiline too -->',
                '    <Version>1.0</Version>',
                '    <Verified xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:nil="true"/>',
                '  </Internal>',
                '</MockConfig>'
        ]
    }

    def 'test that PascalCaseStrategy converts #string to #expected'() {
        given:
        def strategy = new XmlConfigurationAdapter.PascalCaseStrategy()

        when:
        def actual = strategy.translate(string)

        then:
        actual == expected

        where:
        string       || expected
        ''           || ''
        'field_name' || 'FieldName'
        'field-name' || 'FieldName'
        'field.name' || 'FieldName'
        'FieldName'  || 'FieldName'
        'fieldName'  || 'FieldName'
    }

    File getFile(final String name) {
        return new File("build/resources/test/${name}.xml")
    }

    BaseConfigurationAdapter getAdapter() {
        return new XmlConfigurationAdapter(log)
    }

}
