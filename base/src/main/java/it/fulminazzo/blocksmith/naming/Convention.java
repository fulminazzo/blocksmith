package it.fulminazzo.blocksmith.naming;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a naming convention.
 */
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public enum Convention {
    /**
     * camelCase (Java naming convention)
     */
    CAMEL_CASE(new CamelCaseConvention()),
    /**
     * kebab-case
     */
    KEBAB_CASE(new SeparatedCaseConvention("-")),
    /**
     * snake_case
     */
    SNAKE_CASE(new SeparatedCaseConvention("_")),
    /**
     * PascalCase
     */
    PASCAL_CASE(new PascalCaseConvention())
    ;

    @Getter(AccessLevel.PACKAGE)
    @NotNull NamingConvention convention;

}
