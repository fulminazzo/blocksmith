package it.fulminazzo.blocksmith.message.provider

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import net.kyori.adventure.text.Component
import spock.lang.Specification

@Slf4j
class MessageProviderTest extends Specification {
    private static final File baseDir = new File('build/resources/test/translation_message_provider/')

    def 'test creation of TranslationMessageProvider from disk'() {
        given:
        def workingDir = new File(baseDir, 'disk')
        workingDir.deleteDir()
        workingDir.mkdirs()

        and:
        def enUs = new File(workingDir, 'en_us.yml')
        enUs.createNewFile()
        enUs << "message: 'Hello, world!'"

        and:
        def en = new File(workingDir, 'en.yml')
        en.createNewFile()
        en << "message: 'Hello, world!'"

        and:
        def it = new File(workingDir, 'it_it.yml')
        it.createNewFile()
        it << "message: 'Ciao, mondo!'"

        and:
        def undefined = new File(workingDir, 'undefined.yml')
        undefined.createNewFile()
        undefined << "message: 'Gibberish'"

        and:
        new File(workingDir, 'something.json').createNewFile()

        when:
        def provider = MessageProvider.translation(workingDir, '', ConfigurationFormat.YAML, log)

        then:
        provider != null

        when:
        def message = provider.getMessage('message', Locale.US)

        then:
        message == Component.text('Hello, world!')

        when:
        message = provider.getMessage('message', Locale.ITALY)

        then:
        message == Component.text('Ciao, mondo!')
    }

}
