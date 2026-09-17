package it.fulminazzo.blocksmith.application;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Contains all the data of a field.
 *
 * @see FieldInitializerHandler
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(access = AccessLevel.PACKAGE)
public class FieldData {
    @NotNull Field field;
    @NotNull String @NotNull [] dependencies;

}
