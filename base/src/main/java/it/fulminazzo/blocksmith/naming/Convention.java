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
     * The <i>camelCase</i> (Java naming) convention.
     */
    CAMEL_CASE(new CamelCaseConvention()),
    /**
     * The <i>kebab-case</i> convention.
     */
    KEBAB_CASE(new SeparatedCaseConvention("-")),
    /**
     * The <i>snake_case</i> convention.
     */
    SNAKE_CASE(new SeparatedCaseConvention("_")),
    /**
     * The <i>PascalCase</i> convention.
     */
    PASCAL_CASE(new PascalCaseConvention())
    ;

    @Getter(AccessLevel.PACKAGE)
    @NotNull NamingConvention namingConvention;

}
