package it.fulminazzo.blocksmith.message.provider

import net.kyori.adventure.text.Component
import spock.lang.RepeatUntilFailure
import spock.lang.Specification

class MessageProviderTest extends Specification {

    private static final File workDir = new File('build/resources/test/message_provider')

    void setupSpec() {
        workDir.deleteDir()
    }

    @RepeatUntilFailure(maxAttempts = 2)
    def 'test creation of MessageProvider from resource'() {
        when:
        def provider = MessageProvider.resource(workDir, 'messages.yml')

        then:
        provider != null

        when:
        def message = provider.getMessage('message', Locale.ITALIAN)

        then:
        message == Component.text('Hello, world!')
    }

}
