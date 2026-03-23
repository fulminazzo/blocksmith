package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.message.Messenger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class BukkitCommandRegistryFactory implements CommandRegistryFactory {

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull Messenger messenger,
                                                final @NotNull Logger logger,
                                                final @NotNull String prefix) {
        return new BukkitCommandRegistry(messenger, logger, prefix);
    }

}
