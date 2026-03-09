package it.fulminazzo.blocksmith.message;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Data
public final class Player {

    private @NotNull Locale locale = Locale.getDefault();

    private @Nullable Component lastMessage;

}
