package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Marks a class or method as a command declaration for the Blocksmith command framework.
 *
 * <h2>Command syntax</h2>
 * The syntax of the command is defined by the {@link #value()} attribute.
 * It supports the following:
 * <ul>
 *     <li><b>Literals</b> - words used to identify commands and subcommands: {@code info}, {@code member}</li>
 *     <li><b>Aliases</b> - literals separated with {@code |} wrapped in parentheses: {@code (teleport|tp)}</li>
 *     <li><b>Mandatory arguments</b> - command arguments that are <b>required</b>: {@code <player>}</li>
 *     <li><b>Optional arguments</b> - command arguments that do not need to be specified
 *     (however they might be {@code null}): {@code [page]}</li>
 * </ul>
 * Arguments are mapped in declaration order to the method parameters.
 * However, if the first parameter is a <b>command sender type</b>, the actual executor of the command
 * will be mapped to it and the rest of the parameters will be mapped to the remaining arguments.
 * <pre>{@code
 * public class Commands {
 *
 *     @Command("(teleport|tp) <target> <location> [reason]")
 *     public static void teleport(
 *             Player target,
 *             Location location,
 *             String reason
 *     ) {
 *         // execute teleport, do not notify
 *     }
 *
 *     // or
 *
 *     @Command("(teleport|tp) <target> <location> [reason]")
 *     public static void teleport(
 *             CommandSenderWrapper<?> sender,
 *             Player target,
 *             Location location,
 *             String reason
 *     ) {
 *         // execute teleport, notify the sender
 *     }
 *
 * }
 * }</pre>
 * <b>Note</b>: the above will <b>not</b> function correctly. Specifying dual commands will lead
 * to untested behavior. Choose only one of the two options.
 *
 * <h2>Class-level usage</h2>
 * When applied to a class, {@link Command} defines the <b>root command</b> of a command.
 * Each <b>non-static</b> method in the class, annotated with {@link Command} will be considered a subcommand
 * and inherit the root command path.
 * <br>
 * Example:
 * <pre>{@code
 * @Command("clan")
 * public class ClanCommand {
 *
 *     @Command
 *     public void execute(CommandSender sender) {
 *         // This is a special use case of the annotation.
 *         // It allows to define the execution logic of the root command,
 *         // making it executable without subcommands.
 *     }
 *
 *     @Command("info <player>")
 *     public void info(CommandSender sender, Object player) { ... }
 *
 * }
 * }</pre>
 * The above will register the commands:
 * <ul>
 *     <li>{@code /clan}</li>
 *     <li>{@code /clan info <player>}</li>
 * </ul>
 *
 * <h2>Method-level usage</h2>
 * When applied to a method, two cases are possible:
 * <ol>
 *     <li>
 *         If the method <b>is static</b>, then the command will be <b>anonymous</b>, meaning it does not inherit
 *         any root path from a parent command. This is useful for declaring single autonomous commands without
 *         having to create a separate class.
 *         <br>
 *         Example:
 *         <pre>{@code
 *         public class Commands {
 *
 *             @Command("echo <message>")
 *             public static void echo(CommandSender sender, String message) { ... }
 *
 *         }
 *         }</pre>
 *         The above will register the command {@code /echo <message>}.
 *     </li>
 *     <li>
 *         If the method <b>is not static</b>, then the command <b>requires</b> that the method is placed
 *         inside a class annotated with {@link Command}. This is useful for declaring subcommands
 *         with common roots.
 *         <br>
 *         Example:
 *         <pre>{@code
 *         @Command("party")
 *         public class PartyCommand {
 *
 *             @Command("invite <player>")
 *             public void invite(CommandSender sender, Object player) { ... }
 *
 *             @Command("leave")
 *             public void leave(CommandSender sender) { ... }
 *
 *         }
 *         }</pre>
 *         The above will register the commands:
 *         <ul>
 *             <li>{@code /party invite <player>}</li>
 *             <li>{@code /party leave}</li>
 *         </ul>
 *     </li>
 * </ol>
 *
 * <h2>Command sender handling</h2>
 * The command sender is automatically mapped to the first parameter of the method, if specified.
 * The supported types are:
 * <ul>
 *     <li>any command sender class of the current implementation (in <b>Bukkit</b> this would be
 *     {@code CommandSender}, {@code ConsoleCommandSender} and {@code Player});</li>
 *     <li>{@link it.fulminazzo.blocksmith.command.CommandSenderWrapper}: a Blocksmith provided wrapper class.</li>
 * </ul>
 * The framework is intelligent enough to verify the compatibility of the command sender type with
 * the actual sender. For example, if the command is executed by a player, the actual sender will
 * be checked. If it is <b>not</b> a player, then a proper message will be sent stating that
 * only players are allowed to execute the command. Likewise with the console.
 *
 * @see Permission
 * @see Help
 * @see Default
 * @see Greedy
 * @see it.fulminazzo.blocksmith.command.CommandMessages
 * @see it.fulminazzo.blocksmith.command.CommandRegistry
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Command {

    /**
     * The command syntax.
     * Can only be omitted when applied to the <b>execution method</b> of a {@link Command} annotated class.
     *
     * @return the command syntax
     */
    @NotNull String value() default "";

    /**
     * The description of the command.
     * If <b>none</b> is specified, a <b>message code</b> will be computed based on the command route.
     * The code will refer to the <b>path</b> of a message file (or provider) with the actual description.
     * <br>
     * For example, if the command is {@code /clan member <player> promote <rank>},
     * the message code will be {@code command.clan.member.promote.description}.
     *
     * @return the description of the command
     */
    @NotNull String description() default "";

    //TODO: document
    boolean dynamic() default false;

    /**
     * The permission of the command.
     * Check {@link Permission} for more information.
     *
     * @return the permission
     */
    @NotNull Permission permission() default @Permission;

    /**
     * The help subcommand of the command.
     * Check {@link Help} for more information.
     *
     * @return the help subcommand
     */
    @NotNull Help help() default @Help;

}
