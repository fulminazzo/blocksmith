package it.fulminazzo.blocksmith.message.provider

import net.kyori.adventure.text.Component
import spock.lang.Specification

class TranslationMessageProviderImplTest extends Specification {

    private static final MessageProvider enUs = MessageProvider.memory([
            'message1': 'Hello',
            'message2': 'world'
    ])

    private static final MessageProvider itIt = MessageProvider.memory([
            'message1': 'Ciao'
    ])

    private TranslationMessageProvider provider

    void setup() {
        provider = new TranslationMessageProviderImpl()
        provider.registerProvider(Locale.US, enUs)
        provider.registerProvider(Locale.ITALY, itIt)
    }

    def 'test that getMessage of message1 with #locale returns #expected'() {
        when:
        def actual = provider.getMessage('message1', locale)

        then:
        actual == expected

        where:
        locale       || expected
        Locale.US    || Component.text('Hello')
        Locale.ITALY || Component.text('Ciao')
    }

    def 'test that getMessage of message2 with #locale always returns US message'() {
        when:
        def actual = provider.getMessage('message2', locale)

        then:
        actual == Component.text('world')

        where:
        locale << [Locale.US, Locale.ITALY]
    }

    def 'test that getMessage of message3 with #locale throws'() {
        when:
        provider.getMessage('message3', locale)

        then:
        def e = thrown(MessageNotFoundException)
        e.path == 'message3'
        e.locale == locale

        where:
        locale << [Locale.US, Locale.ITALY]
    }

    def 'test that getMessage of not registered locale returns US message'() {
        when:
        def actual = provider.getMessage('message1', Locale.GERMANY)

        then:
        actual == Component.text('Hello')
    }

    def 'test that getMessage of not registered locale and no default locale throws'() {
        given:
        provider.defaultLocale = Locale.CANADA

        when:
        provider.getMessage('message1', Locale.GERMANY)

        then:
        thrown(IllegalArgumentException)
    }

}
