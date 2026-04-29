package it.fulminazzo.blocksmith.command.annotation;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.CommandRegistry;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.message.Messenger;
import it.fulminazzo.blocksmith.validation.annotation.AlphabeticalOrDigit;
import it.fulminazzo.blocksmith.validation.annotation.Size;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * <h1>Command</h1>
 * The {@link Command} annotation marks a class or a method as a command declaration
 * for the Blocksmith command framework.
 * It alleviates most of the boilerplate required for command functioning like
 * argument count checks, validation of arguments and executors, optional arguments
 * and so much more.
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
 *     public void execute(
 *         CommandSenderWrapper<?> sender
 *     ) {
 *         // This is a special use case of the annotation.
 *         // It allows to define the execution logic of the root command,
 *         // making it executable without subcommands.
 *     }
 *
 *     @Command("info <player>")
 *     public void info(
 *         CommandSenderWrapper<?> sender,
 *         Player player
 *     ) { ... }
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
 *             public static void echo(
 *                 CommandSenderWrapper<?> sender,
 *                 String message
 *             ) { ... }
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
 *             public void invite(
 *                 CommandSenderWrapper<?> sender,
 *                 Player player
 *             ) { ... }
 *
 *             @Command("leave")
 *             public void leave(CommandSenderWrapper<?> sender) { ... }
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
 *     <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/command/CommandSender.html">CommandSender</a>,
 *     <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/command/ConsoleCommandSender.html">ConsoleCommandSender</a>
 *     and
 *     <a href="https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Player.html">Player</a>);</li>
 *     <li>{@link CommandSenderWrapper}: a Blocksmith provided wrapper class.</li>
 * </ul>
 * The framework is intelligent enough to verify the compatibility of the command sender type with
 * the actual sender. For example, if the command is executed by a player, the actual sender will
 * be checked. If it is <b>not</b> a player, then a proper message will be sent stating that
 * only players are allowed to execute the command. Likewise with the console.
 *
 * <h2>Arguments</h2>
 * Arguments can either be <b>required</b> or <b>optional</b>.
 * When an <b>optional</b> argument is not specified, the actual value will be {@code null}
 * (therefore, code logic should take that into account, unless the {@link Default} annotation is used).
 * <br>
 * Arguments also support <b>Blocksmith validation</b> thanks to
 * {@link it.fulminazzo.blocksmith.validation.annotation} annotations.
 * The {@code message} value points to the error message sent to the user in case of invalid input,
 * taken from a {@link Messenger} holding all the messages.
 * {@link CommandMessages} documents all available messages with their default codes and supported placeholders.
 * <br>
 * Example:
 * <pre>{@code
 * public class Commands {
 *
 *     @Command("register <email> <password>")
 *     public static void register(
 *         CommandSenderWrapper<?> sender,
 *         @Email String email,
 *         @Size(min = 8, max = Integer.MAX_VALUE) String password
 *     ) {
 *         // To this point the email and password values have already been validated.
 *     }
 *
 * }
 * }</pre>
 * The above will register the command {@code /register <email> <password>}.
 * <br>
 * If the user input had an <b>invalid</b> argument, the following messages will be sent:
 * <ul>
 *     <li>a general {@link CommandMessages#INVALID_ARGUMENTS};</li>
 *     <li>one message for <b>all</b> the violated constraints.</li>
 * </ul>
 * For example, assume a parameter with {@link AlphabeticalOrDigit}
 * and {@link Size} was specified.
 * Inserting a wrong value will send the following messages:
 * <ul>
 *     <li>{@link CommandMessages#INVALID_ARGUMENTS};</li>
 *     <li>{@link AlphabeticalOrDigit#message()}</li>
 *     <li{@link Size#message()}
 * </ul>
 *
 * @see Permission
 * @see Help
 * @see Default
 * @see Greedy
 * @see CommandMessages
 * @see CommandRegistry
 * @see it.fulminazzo.blocksmith.validation
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

    /**
     * <h1>Dynamic Command</h1>
     * A dynamic command is a special type of command that <b>does not require</b> a name (or aliases) specified.
     * This was created to offer the user the possibility to generate commands at <b>runtime</b>
     * with their own aliases of preference.
     * <br>
     * The {@code dynamic} value will have different effects and requirements based on the scope of use.
     *
     * <h2>Class-level usage</h2>
     * When the command was declared as a <b>root command</b>, the containing class <b>requires</b>
     * a <b>no-parameters</b> {@code getAliases} method returning a {@link java.util.Collection} instance.
     * This is where the actual dynamic aliases will be taken from.
     * <br>
     * Example:
     * <pre>{@code
     * @Command(dynamic = true)
     * public class ClanCommand {
     *
     *     public List<String> getAliases() {
     *         // Here the logic may differ as the developer pleases.
     *         // For example, the aliases could be taken from a configuration file.
     *         return Arrays.asList("clan", "gang", "team");
     *     }
     *
     * }
     * }</pre>
     * The above will register the commands:
     * <ul>
     *     <li>{@code /clan}</li>
     *     <li>{@code /gang}</li>
     *     <li>{@code /team}</li>
     * </ul>
     *
     * <h2>Method-level usage</h2>
     * When the {@link Command} annotation is applied to a method, only the <b>static</b> case is supported.
     * This means that, at the time of writing this documentation, it is <b>not</b> possible to declare
     * <b>dynamic subcommands</b>.
     * However, it is possible to declare <b>dynamic anonymous commands</b>.
     * The containing class <b>requires</b> a <b>no-parameters</b> {@code get<capitalized_method_name>Aliases} method
     * returning a {@link java.util.Collection} instance.
     * This is where the actual dynamic aliases will be taken from.
     * <br>
     * Example:
     * <pre>{@code
     * public class Commands {
     *
     *     // Specifying the syntax of the command WITHOUT the aliases
     *     @Command("<message>", dynamic = true)
     *     public static void echo(CommandSenderWrapper<?> sender, String message) { ... }
     *
     *     // The name of this command depends on the name of the method of the command it refers to.
     *     public static List<String> getEchoAliases() {
     *         return Arrays.asList("echo", "print");
     *     }
     *
     * }
     * }</pre>
     * The above will register the commands:
     * <ul>
     *     <li>{@code /echo <message>}</li>
     *     <li>{@code /print <message>}</li>
     * </ul>
     *
     * @return {@code true} if the command should be dynamic
     */
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
