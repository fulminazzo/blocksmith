package it.fulminazzo.blocksmith.message;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Data
@RequiredArgsConstructor
public final class Player {
    public static final @NotNull Collection<Player> ALL_PLAYERS = Arrays.asList(
            new Player("Alex"), new Player("Steve"), new Player("Camilla")
    );

    private final @NotNull String name;

    private final @NotNull Map<TitlePart<?>, Object> lastTitle = new HashMap<>();

    private @NotNull Locale locale = Locale.getDefault();

    private @Nullable Component lastMessage;

}
