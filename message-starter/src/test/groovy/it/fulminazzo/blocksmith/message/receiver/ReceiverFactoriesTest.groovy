package it.fulminazzo.blocksmith.message.receiver

import it.fulminazzo.blocksmith.ServerApplication
import it.fulminazzo.blocksmith.message.Player
import it.fulminazzo.blocksmith.message.UserData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.TitlePart
import org.jetbrains.annotations.NotNull
import org.jspecify.annotations.NonNull
import spock.lang.Specification

import java.util.function.Supplier

class ReceiverFactoriesTest extends Specification {
    private static List<Supplier<ReceiverFactory>> previous

    void setupSpec() {
        previous = [*ReceiverFactories.factories]
        ReceiverFactories.factories.clear()
    }

    void cleanupSpec() {
        ReceiverFactories.factories.clear()
        ReceiverFactories.factories.addAll(previous)
    }

    def 'test that getAllReceivers cannot return two receivers of the same object'() {
        given:
        def player = new Player('Luke')

        and:
        ReceiverFactories.registerCustomFactory(() -> new PlayerReceiverFactory() {

            @Override
            @NotNull Collection<Receiver> getAllReceivers() {
                return [create(player)]
            }

        })

        and:
        ReceiverFactories.registerCustomFactory(() -> new PlayerReceiverFactory() {

            @Override
            @NotNull Collection<Receiver> getAllReceivers() {
                return [create(player)]
            }

            @Override
            <R> Receiver create(final @NotNull R receiver) {
                return new GroovyPlayerReceiver((Player) receiver)
            }

        })

        when:
        def receivers = ReceiverFactories.getAllReceivers(Mock(ServerApplication))

        then:
        receivers.size() == 1

        and:
        def receiver = receivers[0]
        (receiver instanceof GroovyPlayerReceiver)
        receiver.internal == player
    }

    def 'test that registerCustomFactory works'() {
        given:
        def user = new UserData()
        user.locale = Locale.ITALY

        and:
        ReceiverFactories.registerCustomFactory(() -> new UserDataReceiverFactory())

        when:
        def factory = ReceiverFactories.get(user.class, Mock(ServerApplication))

        then:
        factory != null

        when:
        def locale = factory.create(user).locale

        then:
        locale == user.locale
    }

    def 'test that get throws IllegalArgument exception for invalid receiver type'() {
        when:
        ReceiverFactories.get(String, Mock(ServerApplication))

        then:
        thrown(IllegalArgumentException)
    }

    static final class GroovyPlayerReceiver implements Receiver {
        final @NotNull Player internal

        GroovyPlayerReceiver(final @NotNull Player player) {
            this.internal = player
        }

        @Override
        @NotNull Locale getLocale() {
            return internal.locale
        }

        @Override
        @NotNull Audience audience() {
            return new Audience() {

                @Override
                <T> void sendTitlePart(final @NotNull TitlePart<T> part,
                                       final @NotNull T value) {
                    internal.getLastTitle().put(part, value)
                }

                @Override
                void sendMessage(final @NotNull Identity source,
                                 final @NotNull Component message,
                                 final @NotNull MessageType type) {
                    internal.setLastMessage(message)
                }

                @Override
                void sendActionBar(final @NotNull Component message) {
                    internal.setLastMessage(message)
                }

            }
        }

        @NonNull
        @Override
        <R> R handle() {
            return (R) internal;
        }

    }

}
