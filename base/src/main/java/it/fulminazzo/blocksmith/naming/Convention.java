package it.fulminazzo.blocksmith.naming;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a naming convention.
 *
 * @see CaseConverter
 */
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public enum Convention {
    /**
     * The camelCase (Java naming) convention.
     */
    CAMEL_CASE(new CamelCaseConvention()),
    /**
     * The kebab-case convention.
     */
    KEBAB_CASE(new SeparatedCaseConvention("-")),
    /**
     * The snake_case convention.
     */
    SNAKE_CASE(new SeparatedCaseConvention("_")),
    /**
     * The PascalCase convention.
     */
    PASCAL_CASE(new PascalCaseConvention())
    ;

    @Getter(AccessLevel.PACKAGE)
    @NotNull NamingConvention convention;

}
