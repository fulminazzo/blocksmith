package it.fulminazzo.blocksmith.command.annotation;

import it.fulminazzo.blocksmith.command.CommandMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Specifies the execution cooldown for a {@link Command}.
 * The cooldown will only take effect <b>after successful execution</b>
 * meaning that, if the user specified a <b>wrong input</b>, the cooldown will not be applied,
 * offering the possibility to retry the command.
 * <br>
 * If the user attempts to execute the command again before the cooldown expires,
 * the {@link CommandMessages#COMMAND_ON_COOLDOWN} message will be sent.
 * <br>
 * A user may <b>bypass</b> the cooldown with the permission {@code <group>.bypass.cooldown.<permission>}.
 * Imagine a command with full permission {@code blocksmith.command.party.invite}, where {@code blocksmith} is the group.
 * The users with permission {@code blocksmith.bypass.cooldown.command.party.invite} will not be affected by the cooldown.
 * <br>
 * Example:
 * <pre>{@code
 * @Command("party")
 * public class PartyCommand {
 *
 *     @Command("invite <player>")
 *     @Cooldown(value = 1, unit = TimeUnit.MINUTES)
 *     public void invite(
 *         CommandSenderWrapper<?> sender,
 *         Player player
 *     ) { ... }
 *
 * }
 * }</pre>
 * If the annotation is applied to the <b>root command</b> class, it will only refer to its direct execution.
 * It <b>will not</b> affect subcommands.
 * <br>
 * Example:
 * <pre>{@code
 * @Command("friend")
 * @Cooldown(value = 10, unit = TimeUnit.SECONDS)
 * public class FriendCommand {
 *
 *     @Command
 *     public void execute(CommandSenderWrapper<?> sender) { ... }
 *
 *     @Command("add <player>")
 *     public void add(CommandSenderWrapper<?> sender, Player player) { ... }
 *
 * }
 * }</pre>
 * In the above example:
 * <ul>
 *     <li>the {@code /friend} command will be put on cooldown upon execution;</li>
 *     <li>the {@code /friend add <player>} command will be <b>always executable</b> (no cooldown).</li>
 * </ul>
 *
 * <h3>Confirm</h3>
 * If the command is annotated with {@link Confirm}, the cooldown will be applied <b>after the confirmation</b>.
 * Therefore, the user will <b>always</b> be able to execute the command, but only upon confirmation
 * the cooldown will block the execution.
 *
 * @see Command
 * @see Permission
 * @see Confirm
 * @see CommandMessages
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Cooldown {

    /**
     * The cooldown duration.
     *
     * @return the duration
     */
    long value();

    /**
     * The unit used to interpret the {@link #value()}.
     *
     * @return the unit
     */
    @NotNull TimeUnit unit() default TimeUnit.SECONDS;

}
