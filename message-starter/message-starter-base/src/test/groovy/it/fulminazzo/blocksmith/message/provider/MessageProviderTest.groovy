package it.fulminazzo.blocksmith.message.provider

import groovy.util.logging.Slf4j
import net.kyori.adventure.text.Component
import spock.lang.RepeatUntilFailure
import spock.lang.Specification

@Slf4j
class MessageProviderTest extends Specification {

    private static final File workDir = new File('build/resources/test/message_provider')

    void setupSpec() {
        workDir.deleteDir()
    }

    @RepeatUntilFailure(maxAttempts = 2)
    def 'test creation of MessageProvider from resource'() {
        when:
        def provider = MessageProvider.resource(workDir, 'messages.yml', log)

        then:
        provider != null

        when:
        def message = provider.getMessage('message', Locale.ITALIAN)

        then:
        message == Component.text('Hello, world!')
    }

}
