package it.fulminazzo.blocksmith.message;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Data
public final class UserData {

    private @NotNull Locale locale = Locale.getDefault();

}
