package it.fulminazzo.blocksmith.command.annotation;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Specifies the <b>default value</b> for an <b>optional</b> argument.
 * <br>
 * Example:
 * <pre>{@code
 * public class Commands {
 *
 *     @Command("help [page]")
 *     public static void help(
 *         CommandSenderWrapper<?> sender,
 *         @Default("1") Integer page
 *     ) {
 *         // Since the page argument is optional,
 *         // if the sender input was "/help", then the page variable will be equal to 1.
 *         // If the sender input was "/help 2", then the page variable will be equal to 2.
 *         // If the Default annotation was not present, then the page variable would be null.
 *     }
 *
 * }
 * }</pre>
 *
 * @see Command
 * @see Greedy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {

    /**
     * The default value, specified as if it was part of the input.
     * The internal handler will then parse it accordingly, based on the current rules.
     * This means that invalid values will be treated as bad input from the user.
     * <br>
     * For example, if the argument is an {@link Integer} with bounds {@code 1, 4},
     * a default value of {@code 5} will send the user an error message.
     * This also applies to <b>custom completions</b> (check {@link Tab} for more).
     *
     * @see Tab
     * @return the default value
     */
    @NotNull String value();

}
