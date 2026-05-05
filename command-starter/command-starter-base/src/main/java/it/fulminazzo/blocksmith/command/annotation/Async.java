package it.fulminazzo.blocksmith.command.annotation;

import it.fulminazzo.blocksmith.command.CommandMessages;
import it.fulminazzo.blocksmith.command.CommandSenderWrapper;
import it.fulminazzo.blocksmith.scheduler.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Marks a {@link Command} method to be run <b>asynchronously</b>.
 * In some platforms, this annotation will have <b>no effect</b>.
 * <br>
 * When declaring a command as <b>asynchronous</b>, its execution will follow different phases:
 * upon executing the command, the <b>executor</b> is put in a <b>waiting list</b>.
 * If they try to execute the command again <b>before</b> the previous execution is <b>terminated</b>,
 * the {@link CommandMessages#AWAIT_PENDING_OPERATION} error message will be sent instead.
 * If the execution fails <b>before</b> the <b>timeout</b>,
 * the {@link CommandMessages#OPERATION_TIMEOUT} error message will be sent.
 * Otherwise, upon successful execution, the sender will be able to execute the command again.
 * <br>
 * This is useful for controlling commands that may take a long time to complete
 * (like database queries or HTTP requests) where the user should not be able to repeat multiple requests at once.
 * <br>
 * Example:
 * <pre>{@code
 * public class Commands {
 *
 *     @Async(value = 10, unit = TimeUnit.SECONDS)
 *     @Command("image download <url>")
 *     public static void downloadImage(
 *         CommandSenderWrapper<?> sender,
 *         String url
 *     ) {
 *         // To this point the execution is already on an asynchronous thread.
 *     }
 *
 * }
 * }</pre>
 *
 * <h2>command-starter-scheduler</h2>
 * To provide maximum compatibility with the platform in use, this annotation uses the {@link Scheduler}
 * from the {@code command-starter-module} module. This way, asynchronous tasks are properly handled
 * by the platform-specific scheduler.
 *
 * <h2>Bukkit-like platforms</h2>
 * Platforms like <a href="https://hub.spigotmc.org/javadocs/bukkit/">Bukkit</a> require that operations
 * on the server (like interacting with players, entities or the world) are executed <b>synchronously</b>.
 * Therefore, it is advised to return any result obtained from an asynchronous command in the main thread.
 * <br>
 * To do so, {@link CommandSenderWrapper} provides a utility method to act as a shortcut:
 * <pre>{@code
 * public class Commands {
 *
 *     @Async(value = 10, unit = TimeUnit.SECONDS)
 *     @Command("database table <table> get <id>")
 *     public static void getRowFromDatabase(
 *         CommandSenderWrapper<?> sender,
 *         String table,
 *         long id
 *     ) {
 *         Object data = ...; // data obtained from the database
 *         sender.sync(actualSender -> {
 *             // interact with the actual sender on the main thread
 *         });
 *     }
 *
 * }
 * }</pre>
 *
 * @see Command
 * @see CommandMessages
 * @see CommandSenderWrapper
 * @see Scheduler
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Async {

    /**
     * The timeout duration.
     *
     * @return the duration
     */
    long value() default 30;

    /**
     * The unit used to interpret the {@link #value()}.
     *
     * @return the unit
     */
    @NotNull TimeUnit unit() default TimeUnit.SECONDS;

}
