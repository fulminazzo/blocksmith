package it.fulminazzo.blocksmith;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class BlocksmithUser {

    @NotNull UUID uuid;

    @NotNull String username;

    @Nullable Locale locale;

}
