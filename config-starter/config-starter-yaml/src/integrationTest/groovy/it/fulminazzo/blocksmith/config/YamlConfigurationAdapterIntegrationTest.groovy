package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import org.yaml.snakeyaml.comments.CommentLine
import org.yaml.snakeyaml.comments.CommentType
import org.yaml.snakeyaml.nodes.Node

@Slf4j
class YamlConfigurationAdapterIntegrationTest extends ConfigurationAdapterIntegrationTest {

    def 'test that loadComments does not throw for non-root node'() {
        when:
        def actual = adapter.loadComments('\"string\"')

        then:
        actual == [:]
    }

    def 'test that extractComments reads inline comments'() {
        given:
        def node = Mock(Node)
        node.inLineComments >> inlineComments

        when:
        def comments = YamlConfigurationAdapter.extractComments(node)

        then:
        comments == expected

        where:
        inlineComments                                                    || expected
        null                                                              || []
        [
                new CommentLine(null, null, 'Hello', CommentType.BLOCK),
                new CommentLine(null, null, 'mars', CommentType.IN_LINE),
                new CommentLine(null, null, 'world', CommentType.BLOCK)
        ]                                                                 || ['Hello', 'world']
    }

    @Override
    protected boolean supportsNull() {
        return true
    }

    @Override
    protected File getFile(final String name) {
        return new File("build/resources/integrationTest/${name}.yml")
    }

    @Override
    protected BaseConfigurationAdapter getAdapter() {
        return new YamlConfigurationAdapter(log)
    }

    @Override
    protected List<String> getExpectedStoreLines() {
        return [
                '# Example comment',
                'comments-enabled: true',
                '# This comment should be',
                '# Multiline!',
                'name: \'blocksmith\'',
                'description: |-',
                '  This is the description for the configuration file.',
                '  Should be written in multiline format.',
                'authors:',
                '- \'Fulminazzo\'',
                '- \'Camilla\'',
                'internal:',
                '  # This comment should be indented',
                '  # And should be multiline too',
                '  version: 1.0',
                '  verified: null'
        ]
    }

}
