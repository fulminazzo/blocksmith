package it.fulminazzo.blocksmith.command.annotation;

import it.fulminazzo.blocksmith.command.CommandMessages;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Marks a command to <b>require confirmation</b> before actual execution.
 * <br>
 * When using this annotation on a <b>command method</b>, <b>two</b> other <b>literals</b> will be injected
 * for <b>confirmation</b> and <b>cancellation</b>.
 * The execution will <b>never be direct</b>: upon <b>successful</b> execution,
 * the command will require the user to either <b>confirm</b> or <b>cancel</b>
 * the action in order for it to take effect.
 * <br>
 * For example, let's consider the command {@code /ban <player>}.
 * <ul>
 *     <li>when the sender executes {@code /ban <player>}, the execution will be "stored"
 *     for later and the {@link CommandMessages#AWAIT_CONFIRMATION} message will be sent;</li>
 *     <li>the <b>same sender</b> will have to <b>confirm</b> the execution by executing
 *     {@code /ban confirm}, up to which point the player will be effectively banned
 *     (check {@link #confirmAliases()} to configure the command);</li>
 *     <li>if the sender decides to not ban the player, they can either wait for the <b>timeout</b> to end
 *     or they can execute {@code /ban cancel} (check {@link #cancelAliases()} to configure the command).</li>
 * </ul>
 * Note that this process will have effect only if the command execution was <b>successful</b>
 * (if the sender banned a non-existing player, the command would have reported an error and
 * no confirmation would be required).
 *
 * <h3>Cooldown</h3>
 * If the command is annotated with {@link Cooldown}, the confirmation will <b>always be requested</b>.
 * Therefore, the user will <b>always</b> be able to execute the command, but only upon confirmation
 * the cooldown will block the execution.
 *
 * @see Command
 * @see Permission
 * @see Help
 * @see Cooldown
 * @see CommandMessages
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Confirm {

    /**
     * The timeout duration the sender <b>must respect</b> in order to confirm or cancel the action.
     *
     * @return the duration
     */
    long timeout() default 10;

    /**
     * The unit used to interpret the {@link #timeout()}.
     *
     * @return the unit
     */
    @NotNull TimeUnit unit() default TimeUnit.SECONDS;

    /*
     * CONFIRM
     */

    /**
     * The aliases of the confirmation subcommand.
     * At least <b>one</b> must be specified.
     * By default, the confirmation subcommand is named {@code confirm}.
     *
     * @return the aliases of the confirmation subcommand
     */
    @NotNull String @NotNull [] confirmAliases() default {"confirm"};

    /**
     * The description of the confirmation subcommand.
     * If <b>none</b> is specified, a <b>message code</b> will be computed based on the <b>command description</b>.
     * The code will refer to the <b>path</b> of a message file (or provider) with the actual description.
     * <br>
     * For example, if the command is {@code /ban <player>} and its description is {@code command.ban.description},
     * the message code will be {@code command.ban.<first_alias>.description} where
     * {@code <first_alias>} is the first element of {@link #confirmAliases()}
     * (requires the description code to have the {@code .description} suffix).
     *
     * @return the description of the confirmation subcommand
     */
    @NotNull String confirmDescription() default "";

    /**
     * The permission of the confirmation subcommand.
     * If <b>none</b> is specified, the permission is derived from the command.
     * <br>
     * For example, if the command is {@code /ban <player>} and its permission is {@code command.ban},
     * the confirmation subcommand permission will be {@code command.ban.<first_alias>} where
     * {@code <first_alias>} is the first element of {@link #confirmAliases()}.
     *
     * @return the permission of the confirmation subcommand
     * @see Permission
     */
    @NotNull Permission confirmPermission() default @Permission;

    /**
     * The configuration of the help subcommand of the confirmation subcommand.
     * If <b>none</b> is specified, it is computed based on the annotation rules.
     *
     * @return the help subcommand configuration
     * @see Help
     */
    @NotNull Help confirmHelp() default @Help;

    /*
     * CANCEL
     */

    /**
     * The aliases of the cancellation subcommand.
     * At least <b>one</b> must be specified.
     * By default, the cancellation subcommand is named {@code cancel}.
     *
     * @return the aliases of the cancellation subcommand
     */
    @NotNull String @NotNull [] cancelAliases() default {"cancel"};

    /**
     * The description of the cancellation subcommand.
     * If <b>none</b> is specified, a <b>message code</b> will be computed based on the <b>command description</b>.
     * The code will refer to the <b>path</b> of a message file (or provider) with the actual description.
     * <br>
     * For example, if the command is {@code /ban <player>} and its description is {@code command.ban.description},
     * the message code will be {@code command.ban.<first_alias>.description} where
     * {@code <first_alias>} is the first element of {@link #cancelAliases()}
     * (requires the description code to have the {@code .description} suffix).
     *
     * @return the description of the cancellation subcommand
     */
    @NotNull String cancelDescription() default "";

    /**
     * The permission of the cancellation subcommand.
     * If <b>none</b> is specified, the permission is derived from the command.
     * <br>
     * For example, if the command is {@code /ban <player>} and its permission is {@code command.ban},
     * the cancellation subcommand permission will be {@code command.ban.<first_alias>} where
     * {@code <first_alias>} is the first element of {@link #cancelAliases()}.
     *
     * @return the permission of the cancellation subcommand
     * @see Permission
     */
    @NotNull Permission cancelPermission() default @Permission;

    /**
     * The configuration of the help subcommand of the cancellation subcommand.
     * If <b>none</b> is specified, it is computed based on the annotation rules.
     *
     * @return the help subcommand configuration
     * @see Help
     */
    @NotNull Help cancelHelp() default @Help;

}
