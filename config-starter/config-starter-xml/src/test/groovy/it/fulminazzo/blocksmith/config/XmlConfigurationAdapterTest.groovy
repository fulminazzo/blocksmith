package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j

@Slf4j
class XmlConfigurationAdapterTest extends ConfigurationAdapterTest {

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

    @Override
    protected boolean supportsNull() {
        return true
    }

    @Override
    protected File getFile(final String name) {
        return new File("build/resources/test/${name}.xml")
    }

    @Override
    protected BaseConfigurationAdapter getAdapter() {
        return new XmlConfigurationAdapter(log)
    }

    @Override
    protected List<String> getExpectedStoreLines() {
        return [
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

}
