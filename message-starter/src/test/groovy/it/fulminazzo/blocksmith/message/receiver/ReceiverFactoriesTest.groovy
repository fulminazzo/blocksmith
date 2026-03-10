package it.fulminazzo.blocksmith.message.receiver

import it.fulminazzo.blocksmith.message.UserData
import spock.lang.Specification

class ReceiverFactoriesTest extends Specification {

    def 'test that registerCustomFactory works'() {
        given:
        def user = new UserData()
        user.locale = Locale.ITALY

        and:
        ReceiverFactories.registerCustomFactory(new UserDataReceiverFactory())

        when:
        def factory = ReceiverFactories.get(user.class)

        then:
        factory != null

        when:
        def locale = factory.create(user).locale

        then:
        locale == user.locale
    }

}
