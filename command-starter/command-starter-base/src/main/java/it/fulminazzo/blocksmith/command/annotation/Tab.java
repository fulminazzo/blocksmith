package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Special annotation to provide <b>custom tab completions</b> to the argument of a command.
 * When marking a parameter of a command method with this annotation,
 * the framework will replace the default tab completion and validation for the argument
 * with the results of the method specified by the {@link #value()} attribute.
 * <br>
 * This means that executors <b>must</b> provide an argument specified in the tab completions results,
 * otherwise the command will fail.
 * <br>
 * Example:
 * <pre>{@code
 * public class Commands {
 *
 *     @Command("open <book>")
 *     public static void openBook(
 *         CommandSenderWrapper<?> sender,
 *         @Tab("getBooks") String book
 *     ) {
 *         // Both tab completions and execution will be dependent on the method getBooks().
 *         // The "book" argument will be validated against the method returned books.
 *     }
 *
 *     public static List<String> getBooks() {
 *         return Arrays.asList("The Hobbit", "The Lord of the Rings", "Harry Potter");
 *     }
 *
 * }
 * }</pre>
 *
 * @see Command
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Tab {

    /**
     * The name of the method to use to provide tab completions.
     * The method must match some criteria to be used:
     * <ul>
     *     <li>It must have <b>no parameters</b> or at most <b>one</b> (the command executor);</li>
     *     <li>It must return an <b>instance of</b> {@link java.util.Collection}
     *     (the generic type can be anything since the completions will be parsed with {@link Object#toString}).</li>
     * </ul>
     * The lookup process will be performed with the following rules:
     * <ul>
     *     <li>If the method name <b>does not</b> contain a dot {@code .}, 
     *     then it is searched in the same class of the <b>command executor</b>.
     *     However, if the command was <b>not</b> declared as <b>root command</b>,
     *     the method <b>must be static</b> (see {@link Command} for the difference between root and anonymous commands);
     *     </li>
     *     <li>If the method name <b>contains</b> at least one dot {@code .},
     *     then the method is searched in the requested <b>class</b> and <b>must be static</b>.</li>
     * </ul>
     * Examples:
     * <ul>
     *     <li>In root command:
     *     <pre>{@code
     *     @Command("clan")
     *     public class ClanCommand {
     *
     *         @Command("invite <player>")
     *         public void invite(
     *             CommandSenderWrapper<?> sender,
     *             @Tab("getNoClanPlayers") Player player
     *         ) {
     *             // The player is assured to have no clan
     *         }
     *
     *         public List<String> getNoClanPlayers() {
     *             // logic to get players not in a clan
     *             // this method can also be static
     *         }
     *
     *     }
     *     }</pre>
     *     </li>
     *     <li>In anonymous command:
     *     <pre>{@code
     *     public class Commands {
     *
     *         @Command("msg <player>")
     *         public void message(
     *             CommandSenderWrapper<?> sender,
     *             @Tab("getVisiblePlayers") Player player
     *         ) {
     *             // The player is assured to be visible from the sender
     *         }
     *
     *         public static List<String> getVisiblePlayers(CommandSenderWrapper<?> sender) {
     *             // logic to get players that the sender can see
     *             // this method must be static
     *         }
     *
     *     }
     *     }</pre>
     *     </li>
     *     <li>In external class:
     *     <pre>{@code
     *     package it.fulminazzo.blocksmith.util;
     *     
     *     public class Utils {
     *
     *         public static List<String> getVisiblePlayers(CommandSenderWrapper<?> sender) {
     *             // logic to get players that the sender can see
     *         }
     *         
     *     }
     *     }</pre>
     *     <pre>{@code
     *     public class Commands {
     *
     *         @Command("msg <player>")
     *         public void message(
     *             CommandSenderWrapper<?> sender,
     *             @Tab("it.fulminazzo.blocksmith.util.Utils.getVisiblePlayers") Player player
     *         ) {
     *             // The player is assured to be visible from the sender
     *         }
     *
     *     }
     *     }</pre>
     *     </li>
     * </ul>
     *
     * @return the name of the method
     * @see Command
     */
    @NotNull String value();

}
