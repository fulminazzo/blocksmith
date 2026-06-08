package it.fulminazzo.blocksmith.bungee;

import it.fulminazzo.blocksmith.BlocksmithMain;
import it.fulminazzo.blocksmith.ExecutorWrapper;
import it.fulminazzo.blocksmith.ProjectInfo;
import it.fulminazzo.blocksmith.reflect.Reflect;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

import java.util.Arrays;

/**
 * Main entry access point for the plugin.
 *
 * @see BlocksmithMain
 */
public final class Blocksmith extends Plugin {
    private final @NotNull Logger logger = Reflect.on(JDK14LoggerAdapter.class)
            .init(getLogger())
            .get();
    private final @NotNull BlocksmithMain main = new BlocksmithMain(logger);

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerCommand(
                this,
                new Command(ProjectInfo.PROJECT_NAME, null, "bs") {

                    @Override
                    public void execute(final @NotNull CommandSender sender, final @NotNull String[] args) {
                        if (args.length == 0)
                            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "No subcommand specified"));
                        else main.executeCommand(
                                new ExecutorWrapper() {

                                    @Override
                                    public void sendMessage(final @NotNull String message) {
                                        sender.sendMessage(TextComponent.fromLegacyText(message));
                                    }

                                    @Override
                                    public @NotNull String getName() {
                                        return sender.getName();
                                    }

                                },
                                args[0],
                                Arrays.copyOfRange(args, 1, args.length)
                        );
                    }

                }
        );

        main.enable();
    }

    @Override
    public void onDisable() {
        main.disable();
    }

}
