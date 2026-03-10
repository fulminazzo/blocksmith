package it.fulminazzo.blocksmith.message;

import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
public final class Player {

    private final @NotNull Map<TitlePart<?>, Object> lastTitle = new HashMap<>();

    private @NotNull Locale locale = Locale.getDefault();

    private @Nullable Component lastMessage;

}
