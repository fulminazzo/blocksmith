package it.fulminazzo.blocksmith.message.provider

import spock.lang.Specification

class TranslationMessageProviderTest extends Specification {

    def 'test that newProvider with no module throws'() {
        when:
        TranslationMessageProvider.newProvider()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Could not find valid implementation of ${TranslationMessageProvider.simpleName}. " +
                "Please check that the module it.fulminazzo.blocksmith:message-starter-translation " +
                "is correctly installed."
    }

}
