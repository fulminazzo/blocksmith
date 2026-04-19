package it.fulminazzo.blocksmith.message.receiver

import it.fulminazzo.blocksmith.ServerApplication
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class AbstractReceiverFactoryTest extends Specification {

    def 'test that #method with #arguments delegates to implementation'() {
        given:
        def factory = Spy(MockReceiverFactory)
        factory.setup(Mock(ServerApplication))
        factory."$method"(_) >> {
            callRealMethod()
        }

        when:
        factory."$method"(*arguments)

        then:
        1 * factory."${method}Impl"(*_) >> { a ->
            assert a == arguments
        }

        where:
        method            | arguments
        'getAllReceivers' | []
        'create'          | [new Object()]
        'supports'        | [Object]
    }

    def 'test that #method with #arguments throws if not initialized'() {
        given:
        def factory = Spy(MockReceiverFactory)
        factory."$method"(_) >> {
            callRealMethod()
        }

        when:
        factory."$method"(*arguments)

        then:
        def e = thrown(IllegalStateException)
        e.message =~ ".*${ReceiverFactory.simpleName}#setup.*"

        where:
        method            | arguments
        'getAllReceivers' | []
        'create'          | [new Object()]
        'supports'        | [Object]
    }

    final static class MockReceiverFactory extends AbstractReceiverFactory {

        @Override
        protected @NotNull
        Collection<Receiver> getAllReceiversImpl() {
            throw new UnsupportedOperationException()
        }

        @Override
        @NotNull
        protected <R> Receiver createImpl(@NotNull R receiver) {
            throw new UnsupportedOperationException()
        }

        @Override
        protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
            throw new UnsupportedOperationException()
        }

    }

}
