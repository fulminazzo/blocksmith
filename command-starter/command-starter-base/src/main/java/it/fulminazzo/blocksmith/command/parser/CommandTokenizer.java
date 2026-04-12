package it.fulminazzo.blocksmith.command.parser;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A tokenizer for {@link CommandToken} tokens.
 */
@RequiredArgsConstructor
final class CommandTokenizer {
    private final @NotNull InputStream stream;
    private @Nullable CommandToken lastToken;
    private @Nullable String lastRead;
    private @Nullable Character buffer;

    /**
     * Instantiates a new Command tokenizer.
     *
     * @param input the input
     */
    public CommandTokenizer(final @NotNull String input) {
        this.stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Gets the next token from the input.
     *
     * @return the token
     */
    public @NotNull CommandToken next() {
        try {
            String tmp = "";
            if (buffer != null) {
                tmp = buffer.toString();
                buffer = null;
                lastRead = tmp;
            }
            CommandToken commandToken = CommandToken.getToken(tmp);
            int c;
            while ((c = stream.read()) != -1) {
                tmp += (char) c;
                CommandToken nextToken = CommandToken.getToken(tmp);
                if (commandToken == null) commandToken = nextToken;
                else if (commandToken != nextToken) {
                    buffer = (char) c;
                    break;
                }
                lastRead = tmp;
            }
            if (commandToken == null) commandToken = CommandToken.EOF;
            lastToken = commandToken;
            return commandToken;
        } catch (IOException e) {
            throw new TimeParseException("Could not read next token", e);
        }
    }

    /**
     * Gets the last read token.
     *
     * @return the token
     */
    public @NotNull CommandToken getLastToken() {
        if (lastToken == null)
            throw new IllegalStateException(String.format("No token has been read yet. Please use %s#next to start reading",
                    getClass().getSimpleName()
            ));
        return lastToken;
    }

    /**
     * Gets the last read input.
     *
     * @return the input
     */
    public @NotNull String getLastRead() {
        if (lastRead == null)
            throw new IllegalStateException(String.format("No token has been read yet. Please use %s#next to start reading",
                    getClass().getSimpleName()
            ));
        return lastRead;
    }

}
