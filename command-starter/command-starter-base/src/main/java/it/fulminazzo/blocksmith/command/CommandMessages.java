package it.fulminazzo.blocksmith.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * This file stores all the <b>message codes</b> used throughout the framework to display information
 * to a command executor. The codes represent the entries of a {@link it.fulminazzo.blocksmith.message.Messenger}
 * (assuming the actual real messages were provided) to allow for easier customization from the end user.
 * <br>
 * Each message code supports different <b>placeholders</b> which will be replaced by some value
 * (read the documentation of an entry to understand which one).
 * Placeholders are provided with {@link it.fulminazzo.blocksmith.message.argument.Placeholder}
 * (time placeholders use {@link it.fulminazzo.blocksmith.message.argument.Time} instead).
 * <br>
 * Also check out {@link it.fulminazzo.blocksmith.validation.ValidationMessages} 
 * for the messages sent for invalid arguments.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandMessages {

    /**
     * The user does not have permission to execute the command.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>permission</b>: the required (missing) permission.</li>
     * </ul>
     */
    public static final @NotNull String NO_PERMISSION = "error.no-permission";

    /**
     * The given literal subcommand was not recognized.
     * <br>
     * Placeholders:
     * <ul>
     *     <li>{@link #ARGUMENT_PLACEHOLDER}: the unrecognized command.</li>
     * </ul>
     */
    public static final @NotNull String COMMAND_NOT_FOUND = "error.command-not-found";

    /**
     * The requested player could not be found.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>player</b>: the requested player.</li>
     * </ul>
     */
    public static final @NotNull String PLAYER_NOT_FOUND = "error.player-not-found";

    /**
     * The requested server could not be found.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>server</b>: the requested server.</li>
     * </ul>
     */
    public static final @NotNull String SERVER_NOT_FOUND = "error.server-not-found";

    /**
     * The requested world could not be found.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>world</b>: the requested world.</li>
     * </ul>
     */
    public static final @NotNull String WORLD_NOT_FOUND = "error.world-not-found";

    /**
     * Players cannot execute the command.
     */
    public static final @NotNull String PLAYER_CANNOT_EXECUTE = "error.player-cannot-execute";

    /**
     * Console cannot execute the command.
     */
    public static final @NotNull String CONSOLE_CANNOT_EXECUTE = "error.console-cannot-execute";

    /**
     * The arguments specified were not enough to execute the command.
     */
    public static final @NotNull String NOT_ENOUGH_ARGUMENTS = "error.not-enough-arguments";

    /**
     * The argument should have been a <b>number</b> but was something else.
     * <br>
     * Placeholders:
     * <ul>
     *     <li>{@link #ARGUMENT_PLACEHOLDER}: the given invalid value;</li>
     *     <li><b>min</b>: the minimum allowed value;</li>
     *     <li><b>max</b>: the maximum allowed value.</li>
     * </ul>
     */
    public static final @NotNull String INVALID_NUMBER = "error.invalid-number";

    /**
     * The argument should have been a <b>character</b> but was something else.
     * <br>
     * Placeholders:
     * <ul>
     *     <li>{@link #ARGUMENT_PLACEHOLDER}: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String INVALID_CHARACTER = "error.invalid-character";

    /**
     * The argument should have been a <b>boolean</b> but was something else.
     * <br>
     * Placeholders:
     * <ul>
     *     <li>{@link #ARGUMENT_PLACEHOLDER}: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String INVALID_BOOLEAN = "error.invalid-boolean";

    /**
     * The argument should have been a {@link java.util.Locale} but was something else.
     * <br>
     * Placeholders:
     * <ul>
     *     <li>{@link #ARGUMENT_PLACEHOLDER}: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String INVALID_LOCALE = "error.invalid-locale";

    /**
     * The argument was supposed to be the entry of an <b>enum</b> but was something else.
     * <br>
     * Placeholders:
     * <ul>
     *     <li>{@link #ARGUMENT_PLACEHOLDER}: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String INVALID_ENUM = "error.invalid-enum";

    /**
     * The arguments specified could not be validated.
     * After this message, follow-up messages will be sent displaying
     * each invalid argument with its problems.
     */
    public static final @NotNull String INVALID_ARGUMENTS = "error.invalid-arguments";

    /**
     * The given argument was not recognized among the ones specified
     * with {@link it.fulminazzo.blocksmith.command.annotation.Tab}.
     * <br>
     * Placeholders:
     * <ul>
     *     <li>{@link #ARGUMENT_PLACEHOLDER}: the given invalid value;</li>
     *     <li><b>expected</b>: the expected values (specified through the annotation).</li>
     * </ul>
     */
    public static final @NotNull String UNRECOGNIZED_ARGOMENT = "error.unrecognized-argument";

    /**
     * The command was executed while another command was still pending a result.
     * It will be available once the previous one is done.
     * (Only happens for {@link it.fulminazzo.blocksmith.command.annotation.Async} commands.)
     */
    public static final @NotNull String AWAIT_PENDING_OPERATION = "error.await-pending-operation";

    /**
     * It was not possible to complete the command execution in time.
     * (Only happens for {@link it.fulminazzo.blocksmith.command.annotation.Async} commands.)
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>time</b>: the timeout.
     *     Parsed with {@link it.fulminazzo.blocksmith.message.argument.time.parser.TimeParser}.</li>
     * </ul>
     */
    public static final @NotNull String OPERATION_TIMEOUT = "error.operation-timeout";

    /**
     * The command execution was successful,
     * but a confirmation is required for changes to take effect.
     * (Only happens for {@link it.fulminazzo.blocksmith.command.annotation.Confirm} commands.)
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>time</b>: the time in which the command must be confirmed.
     *     Parsed with {@link it.fulminazzo.blocksmith.message.argument.time.parser.TimeParser}.</li>
     * </ul>
     */
    public static final @NotNull String AWAIT_CONFIRMATION = "general.await-confirmation";

    /**
     * The user canceled a command that required confirmation.
     * (Only happens for {@link it.fulminazzo.blocksmith.command.annotation.Confirm} commands.)
     */
    public static final @NotNull String PENDING_ACTION_CANCELLED = "success.pending-action-cancelled";

    /**
     * The user tried to confirm or cancel a command out of its limit execution time.
     * (Only happens for {@link it.fulminazzo.blocksmith.command.annotation.Confirm} commands.)
     */
    public static final @NotNull String PENDING_ACTION_EXPIRED = "error.pending-action-expired";

    /**
     * The user tried to confirm or cancel a command that was never executed.
     * (Only happens for {@link it.fulminazzo.blocksmith.command.annotation.Confirm} commands.)
     */
    public static final @NotNull String NO_PENDING_ACTION = "error.no-pending-action";

    /**
     * The command is on cooldown.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>cooldown</b>: the time before it can be executed again.
     *     Parsed with {@link it.fulminazzo.blocksmith.message.argument.time.parser.TimeParser}.</li>
     * </ul>
     */
    public static final @NotNull String COMMAND_ON_COOLDOWN = "error.command-on-cooldown";

    /**
     * An internal error occurred while executing the command.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>message</b>: the error message (of the exception).</li>
     * </ul>
     */
    public static final @NotNull String INTERNAL_ERROR = "error.internal-error";


    /**
     * Identifies the path where to find the configurable description of a command.
     */
    public static final @NotNull String COMMAND_DESCRIPTION = "command.%s.description";

    /**
     * Identifies the text to prepend before a command permission in the help page.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>name</b>: the name of the command;</li>
     *     <li><b>description</b>: the description of the command;</li>
     *     <li><b>permission</b>: the permission of the command;</li>
     *     <li><b>usage</b>: the usage of the command.</li>
     * </ul>
     */
    public static final @NotNull String HELP_COMMAND_PERMISSION = "command.help.permission";

    /**
     * Identifies the text to prepend before a command usage in the help page.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>name</b>: the name of the command;</li>
     *     <li><b>description</b>: the description of the command;</li>
     *     <li><b>permission</b>: the permission of the command;</li>
     *     <li><b>usage</b>: the usage of the command.</li>
     * </ul>
     */
    public static final @NotNull String HELP_COMMAND_USAGE = "command.help.usage";

    /**
     * Identifies the title of the separator displayed in the help page of a command.
     */
    public static final @NotNull String HELP_COMMAND_SUBCOMMANDS = "command.help.subcommands";

    /**
     * Identifies the format with which to display a subcommand in the help page of a command.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>name</b>: the name of the subcommand;</li>
     *     <li><b>description</b>: the description of the subcommand;</li>
     *     <li><b>permission</b>: the permission of the subcommand;</li>
     *     <li><b>usage</b>: the usage of the subcommand.</li>
     * </ul>
     */
    public static final @NotNull String HELP_COMMAND_SUBCOMMAND_FORMAT = "command.help.subcommand-format";

    /**
     * Text rendered when no subcommand is available.
     * Check {@link it.fulminazzo.blocksmith.command.help.HelpPageRenderer} to see
     * how many lines are available.
     */
    public static final @NotNull String HELP_COMMAND_NO_SUBCOMMANDS = "command.help.no-subcommands";

    /**
     * Identifies the name of the previous page button.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>page</b>: the page number;</li>
     *     <li><b>pages</b>: the total pages.</li>
     * </ul>
     */
    public static final @NotNull String HELP_COMMAND_PREVIOUS_PAGE = "command.help.previous-page";

    /**
     * Identifies the name of the current page text.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>page</b>: the page number;</li>
     *     <li><b>pages</b>: the total pages.</li>
     * </ul>
     */
    public static final @NotNull String HELP_COMMAND_CURRENT_PAGE = "command.help.current-page";

    /**
     * Identifies the name of the next page button.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>page</b>: the page number;</li>
     *     <li><b>pages</b>: the total pages.</li>
     * </ul>
     */
    public static final @NotNull String HELP_COMMAND_NEXT_PAGE = "command.help.next-page";


    /**
     * The placeholder used throughout the messages to replace the current argument.
     */
    public static final @NotNull String ARGUMENT_PLACEHOLDER = "argument";

}
