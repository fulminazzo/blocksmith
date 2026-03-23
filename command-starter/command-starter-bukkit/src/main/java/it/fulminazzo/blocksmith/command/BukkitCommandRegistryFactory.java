package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.BlocksmithApplication;
import org.jetbrains.annotations.NotNull;

public final class BukkitCommandRegistryFactory implements CommandRegistryFactory {

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull BlocksmithApplication application) {
        return new BukkitCommandRegistry(application);
    }

}
