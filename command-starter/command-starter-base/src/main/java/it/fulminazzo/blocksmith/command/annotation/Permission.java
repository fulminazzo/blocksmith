package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Allows to <b>manually</b> define the permission of execution for a command.
 * <br>
 * If this annotation is <b>not</b> provided, by default the command will have the following permission:
 * <ul>
 *     <li>{@code group}: the name of the {@link it.fulminazzo.blocksmith.ServerApplication}
 *     that initialized the command, in lower case. This usually refers to the plugin name;</li>
 *     <li>{@code value}: this value is computed dynamically, based on the <b>literals</b>
 *     of the command divided by a dot {@code .}.
 *     For example, if the command is {@code /clan member <player> promote <rank>},
 *     the value will be {@code clan.member.promote};</li>
 *     <li>{@code grant}: {@link Grant#OP}.</li>
 * </ul>
 *
 * @see Command
 * @see it.fulminazzo.blocksmith.ServerApplication
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Permission {

    /**
     * The actual permission required to execute the command, stripped down of its {@link #group()}.
     *
     * @return the permission
     */
    @NotNull String value() default "";

    /**
     * The group that unifies all the permissions of a project.
     * Ideally, this should be the lower case name of the plugin and it should be uniform across all commands.
     * However, there are no restrictions on this value.
     *
     * @return the group
     */
    @NotNull String group() default "";

    /**
     * Defines who the permission is granted to (by default).
     *
     * @return the grant
     */
    @NotNull Grant grant() default Grant.OP;

    /**
     * Defines who the permission is granted to (unless overrides).
     */
    enum Grant {
        /**
         * Everybody can use the command.
         */
        ALL,
        /**
         * Operators will be able to use the command by default.
         * This only works on <a href="https://hub.spigotmc.org/javadocs/bukkit/">Bukkit</a> platforms.
         */
        OP,
        /**
         * Nobody can use the command unless they have explicit permission for it.
         */
        NONE
    }

}
