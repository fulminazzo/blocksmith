package it.fulminazzo.blocksmith.message;

import it.fulminazzo.blocksmith.message.argument.Argument;
import it.fulminazzo.blocksmith.message.argument.Placeholder;
import it.fulminazzo.blocksmith.message.provider.MessageNotFoundException;
import it.fulminazzo.blocksmith.message.provider.MessageProvider;
import it.fulminazzo.blocksmith.message.receiver.Receiver;
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactories;
import it.fulminazzo.blocksmith.message.receiver.ReceiverFactory;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Handles all the messages and translations to send to users.
 */
@RequiredArgsConstructor
public final class Messenger {
    private final @NotNull Logger logger;

    private @Nullable MessageProvider messageProvider;

    /**
     * Broadcasts a message to all the available receivers through titles.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param titleCode    the title message code
     * @param subtitleCode the subtitle message code
     * @param arguments    the arguments to apply to the message
     */
    public void broadcastTitle(final @Nullable String titleCode,
                               final @Nullable String subtitleCode,
                               final Argument @NotNull ... arguments) {
        ReceiverFactories.getAllReceivers().forEach(r -> sendTitle(r, titleCode, subtitleCode, arguments));
    }

    /**
     * Broadcasts a message to all the available receivers through titles.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param titleCode    the title message code
     * @param subtitleCode the subtitle message code
     * @param times        the timings of the title
     * @param arguments    the arguments to apply to the message
     */
    public void broadcastTitle(final @Nullable String titleCode,
                               final @Nullable String subtitleCode,
                               final @NotNull Title.Times times,
                               final Argument @NotNull ... arguments) {
        ReceiverFactories.getAllReceivers().forEach(r -> sendTitle(r, titleCode, subtitleCode, times, arguments));
    }

    /**
     * Broadcasts a message to all the available receivers through action bar.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param messageCode the message code
     * @param arguments   the arguments to apply to the message
     */
    public void broadcastActionBar(final @NotNull String messageCode,
                                   final Argument @NotNull ... arguments) {
        ReceiverFactories.getAllReceivers().forEach(r -> sendActionBar(r, messageCode, arguments));
    }

    /**
     * Broadcasts a message to all the available receivers through chat.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param messageCode the message code
     * @param arguments   the arguments to apply to the message
     */
    public void broadcastMessage(final @NotNull String messageCode,
                                 final Argument @NotNull ... arguments) {
        ReceiverFactories.getAllReceivers().forEach(r -> sendMessage(r, messageCode, arguments));
    }

    /**
     * Sends a message to the given receiver through titles.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param <R>          the type of the receiver
     * @param receiver     the receiver
     * @param titleCode    the title message code
     * @param subtitleCode the subtitle message code
     * @param arguments    the arguments to apply to the message
     */
    public <R> void sendTitle(final @NotNull R receiver,
                              final @Nullable String titleCode,
                              final @Nullable String subtitleCode,
                              final Argument @NotNull ... arguments) {
        sendTitle(
                receiver,
                titleCode,
                subtitleCode,
                Title.Times.times(
                        Duration.of(1L, ChronoUnit.SECONDS),
                        Duration.of(2L, ChronoUnit.SECONDS),
                        Duration.of(1L, ChronoUnit.SECONDS)
                ),
                arguments
        );
    }

    /**
     * Sends a message to the given receiver through titles.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param <R>          the type of the receiver
     * @param receiver     the receiver
     * @param titleCode    the title message code
     * @param subtitleCode the subtitle message code
     * @param times        the timings of the title
     * @param arguments    the arguments to apply to the message
     */
    public <R> void sendTitle(final @NotNull R receiver,
                              final @Nullable String titleCode,
                              final @Nullable String subtitleCode,
                              final @NotNull Title.Times times,
                              final Argument @NotNull ... arguments) {
        if (titleCode == null && subtitleCode == null) return;
        ReceiverFactory factory = ReceiverFactories.get(receiver.getClass());
        Receiver rec = factory.create(receiver);
        rec.toAudience().sendTitlePart(TitlePart.TIMES, times);
        if (subtitleCode != null)
            sendMessageHelper((a, c) -> a.sendTitlePart(TitlePart.SUBTITLE, c), receiver, subtitleCode, arguments);
        if (titleCode != null)
            sendMessageHelper((a, c) -> a.sendTitlePart(TitlePart.TITLE, c), receiver, titleCode, arguments);
    }

    /**
     * Sends a message to the given receiver through action bar.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param <R>         the type of the receiver
     * @param receiver    the receiver
     * @param messageCode the message code
     * @param arguments   the arguments to apply to the message
     */
    public <R> void sendActionBar(final @NotNull R receiver,
                                  final @NotNull String messageCode,
                                  final Argument @NotNull ... arguments) {
        sendMessageHelper(Audience::sendActionBar, receiver, messageCode, arguments);
    }

    /**
     * Sends a message to the given receiver through chat.
     * If the message could not be found, a warning will be displayed
     * and no error will be returned.
     *
     * @param <R>         the type of the receiver
     * @param receiver    the receiver
     * @param messageCode the message code
     * @param arguments   the arguments to apply to the message
     */
    public <R> void sendMessage(final @NotNull R receiver,
                                final @NotNull String messageCode,
                                final Argument @NotNull ... arguments) {
        sendMessageHelper(Audience::sendMessage, receiver, messageCode, arguments);
    }

    private <R> void sendMessageHelper(final @NotNull BiConsumer<Audience, Component> function,
                                       final @NotNull R receiver,
                                       final @NotNull String messageCode,
                                       final Argument @NotNull ... arguments) {
        try {
            ReceiverFactory factory = ReceiverFactories.get(receiver.getClass());
            Receiver rec = factory.create(receiver);
            Locale locale = rec.getLocale();
            Component message = getComponent(messageCode, locale, arguments);
            function.accept(rec.toAudience(), message);
        } catch (MessageNotFoundException e) {
            logger.warn(e.getMessage());
        }
    }

    /**
     * Gets a Text Adventure component from the given message code.
     * Uses the internal {@link MessageProvider}.
     *
     * @param messageCode the message code
     * @param locale      the locale
     * @param arguments   the arguments to apply to the message
     * @return the component (if found)
     */
    public @Nullable Component getComponentOrNull(final @NotNull String messageCode,
                                                  final @NotNull Locale locale,
                                                  final Argument @NotNull ... arguments) {
        try {
            return getComponent(messageCode, locale, arguments);
        } catch (MessageNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets a Text Adventure component from the given message code.
     * Uses the internal {@link MessageProvider}.
     *
     * @param messageCode the message code
     * @param locale      the locale
     * @param arguments   the arguments to apply to the message
     * @return the component
     * @throws MessageNotFoundException in case the message code was invalid
     */
    public @NotNull Component getComponent(final @NotNull String messageCode,
                                           final @NotNull Locale locale,
                                           final Argument @NotNull ... arguments) throws MessageNotFoundException {
        Component message = getMessageProvider().getMessage(messageCode, locale);
        for (Argument argument : arguments) message = argument.apply(message);

        if (!messageCode.equals("prefix")) {
            Component prefix = getComponentOrNull("prefix", locale);
            if (prefix == null) prefix = Component.empty();
            message = Placeholder.of("prefix", prefix).apply(message);
        }

        return message;
    }

    /**
     * Sets the internal message provider.
     *
     * @param messageProvider the message provider
     * @return this object (for method chaining)
     */
    public @NotNull Messenger setMessageProvider(final @Nullable MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
        return this;
    }

    private @NotNull MessageProvider getMessageProvider() {
        if (messageProvider == null)
            throw new IllegalStateException("No message provider has been specified yet. " +
                    String.format("Please use %s#setMessageProvider(%s)",
                            Messenger.class.getSimpleName(),
                            MessageProvider.class.getSimpleName()
                    )
            );
        return messageProvider;
    }

}
