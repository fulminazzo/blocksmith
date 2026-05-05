package it.fulminazzo.blocksmith.command.annotation;

import java.lang.annotation.*;

/**
 * Marks the parameter of a command method as greedy.
 * A <b>greedy argument</b> will take <b>all the remaining input</b> as final value.
 * Therefore, <b>only one</b> greedy argument is allowed.
 * <br>
 * Example:
 * <pre>{@code
 * public class Commands {
 *
 *     @Command("(message|msg|m) <player> <message>")
 *     public static void message(
 *         CommandSenderWrapper<?> sender,
 *         Player receiver,
 *         @Greedy String message
 *     ) {
 *         // If the sender input was "/msg Fulminazzo Hello, friend! How was your day?"
 *         // The message variable will contain "Hello, friend! How was your day?"
 *     }
 *
 * }
 * }</pre>
 *
 * @see Command
 * @see Default
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Greedy {

}
