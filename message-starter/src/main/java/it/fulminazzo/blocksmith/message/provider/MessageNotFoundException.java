package it.fulminazzo.blocksmith.message.provider;

import it.fulminazzo.blocksmith.message.util.LocaleUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class MessageNotFoundException extends Exception {
    private static final long serialVersionUID = 554883217523102581L;

    @NotNull String path;
    @NotNull Locale locale;

    /**
     * Instantiates a new Message not found exception.
     *
     * @param path   the path
     * @param locale the locale
     */
    public MessageNotFoundException(final @NotNull String path,
                                    final @NotNull Locale locale) {
        super(String.format("Could not find message with path '%s' and locale '%s'", path, LocaleUtils.toString(locale)));
        this.path = path;
        this.locale = locale;
    }

}
