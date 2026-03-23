package it.fulminazzo.blocksmith.command;

import it.fulminazzo.blocksmith.BlocksmithApplication;
import org.jetbrains.annotations.NotNull;

public final class BungeeCommandRegistryFactory implements CommandRegistryFactory {

    @Override
    public @NotNull CommandRegistry newRegistry(final @NotNull BlocksmithApplication application) {
        return new BungeeCommandRegistry(application);
    }

}
