package it.fulminazzo.blocksmith.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Data
@AllArgsConstructor
public final class MessageParseContext {
    private final @NotNull Messenger messenger;
    private final @NotNull Locale locale;

    private @NotNull Component message;

}
