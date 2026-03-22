package it.fulminazzo.blocksmith.command.parser;

import lombok.Getter;
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
    @Getter
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
            if (stream.available() == 0) return CommandToken.EOF;
            String tmp = "";
            if (buffer != null) tmp = buffer.toString();
            CommandToken commandToken = CommandToken.getToken(tmp);
            int c;
            while ((c = stream.read()) != -1) {
                tmp += (char) c;
                CommandToken nextToken = CommandToken.getToken(tmp);
                if (commandToken == null) commandToken = nextToken;
                else if (commandToken != nextToken) {
                    buffer = (char) c;
                    return commandToken;
                }
                lastRead = tmp;
            }
            if (commandToken == null) return CommandToken.EOF;
            else return commandToken;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
