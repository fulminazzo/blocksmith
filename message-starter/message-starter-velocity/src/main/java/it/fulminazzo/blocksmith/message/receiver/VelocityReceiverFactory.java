package it.fulminazzo.blocksmith.message.receiver;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import it.fulminazzo.blocksmith.ServerApplication;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link ReceiverFactory} for Velocity platform.
 */
public final class VelocityReceiverFactory extends AbstractReceiverFactory {
    private ProxyServer server;

    @Override
    public @NotNull ReceiverFactory setup(final @NotNull ServerApplication application) {
        super.setup(application);
        server = application.server();
        return this;
    }

    @Override
    protected @NotNull Collection<Receiver> getAllReceiversImpl() {
        return Stream.concat(
                server.getAllPlayers().stream(),
                Stream.of(server.getConsoleCommandSource())
        ).map(this::create).collect(Collectors.toList());
    }

    @Override
    protected @NotNull <R> Receiver createImpl(final @NotNull R receiver) {
        return new VelocityReceiver((CommandSource) receiver);
    }

    @Override
    protected boolean supportsImpl(final @NotNull Class<?> receiverType) {
        return CommandSource.class.isAssignableFrom(receiverType);
    }

}
