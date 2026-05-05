package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Overrides the default <b>help subcommand</b> configuration.
 * <br>
 * If this annotation is <b>not</b> provided, by default <b>every literal</b> will have
 * a {@code help} subcommand to display information about it (description, permission,
 * usage and subcommands). The command will have the following parameters:
 * <ul>
 *     <li>{@code aliases}: {@code help};</li>
 *     <li>{@code description}: computed from the command it refers to.
 *     For example, if the command is {@code /clan member <player> promote <rank>} and its
 *     description is {@code command.clan.member.promote.description}, the help command description will be
 *     {@code command.clan.member.promote.help.description} (requires the description code to have the
 *     {@code .description} suffix);</li>
 *     <li>{@code permission}: similarly to {@code description}, the permission is derived from the command.
 *     For example, if the command is {@code /clan member <player> promote <rank>} and its
 *     permission is {@code command.clan.member.promote}, the help command permission will be
 *     {@code command.clan.member.promote.help}.
 *     </li>
 * </ul>
 *
 * @see Command
 * @see Permission
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Help {
    /**
     * The default name of the help subcommand.
     */
    @NotNull String DEFAULT_NAME = "help";

    /**
     * The aliases of the help subcommand.
     * At least <b>one</b> must be specified.
     *
     * @return the aliases of the help subcommand
     */
    @NotNull String @NotNull [] aliases() default {DEFAULT_NAME};

    /**
     * The description of the help subcommand.
     * If <b>none</b> is specified, a <b>message code</b> will be computed based on the <b>command description</b>.
     * The code will refer to the <b>path</b> of a message file (or provider) with the actual description.
     * <br>
     * For example, if the command is {@code /clan member <player> promote <rank>} and
     * its description is {@code command.clan.member.promote.description},
     * the message code will be {@code command.clan.member.promote.help.description}.
     *
     * @return the description of the help subcommand
     */
    @NotNull String description() default "";

    /**
     * The permission of the help subcommand.
     * Check {@link Permission} for more information.
     *
     * @return the permission of the help subcommand
     * @see Permission
     */
    @NotNull Permission permission() default @Permission;

}
