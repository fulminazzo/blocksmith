package it.fulminazzo.blocksmith.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * This file stores all the default <b>message codes</b> used by the {@link Constraint} annotations.
 * The codes represent the entries of a <b>configuration file</b> containing the real messages
 * to allow for easier customization from the end user.
 * <br>
 * Each message code supports different <b>placeholders</b> which will be replaced by some value
 * (read the documentation of an entry to understand which one).
 * Placeholders are expected to be in the form <code>%&lt;placeholder&gt;%</code>.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ValidationMessages {

    /**
     * The annotation expected a Java class, but another instance was given.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>expected</b>: the expected type(s) name.</li>
     * </ul>
     */
    public static final @NotNull String INVALID_TYPE = "error.validation.invalid-type";

    /**
     * The annotation expected the value to be <b>not</b> <code>null</code>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_NOT_NULL = "error.validation.not-null";

    /**
     * The annotation expected the value to be <code>true</code>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_TRUE = "error.validation.required-true";

    /**
     * The annotation expected the value to be <code>false</code>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_FALSE = "error.validation.required-false";

    /**
     * The annotation expected the value to be a <b>number</b> or <b>duration</b> less than another.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>expected</b>: the (included) maximum number or duration allowed.</li>
     * </ul>
     */
    public static final @NotNull String NUMBER_TOO_BIG = "error.validation.number-too-big";

    /**
     * The annotation expected the value to be a <b>number</b> or <b>duration</b> greater than another.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>expected</b>: the (included) minimum number or duration allowed.</li>
     * </ul>
     */
    public static final @NotNull String NUMBER_TOO_SMALL = "error.validation.number-too-small";

    /**
     * The annotation expected the value to be a <b>number</b> or <b>duration</b>
     * greater than a number and less than another number.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>min</b>: the (included) minimum number or duration allowed;</li>
     *     <li><b>max</b>: the (included) maximum number or duration allowed.</li>
     * </ul>
     */
    public static final @NotNull String NUMBER_EXCEEDS_RANGE = "error.validation.number-exceeds-range";

    /**
     * The annotation expected the value to be a <b>negative number</b> or <b>duration</b>.
     * <code>0</code> is <b>not</b> allowed.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_NEGATIVE = "error.validation.negative";

    /**
     * The annotation expected the value to be a <b>negative number</b> or <b>duration</b>.
     * <code>0</code> is allowed.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_NEGATIVE_OR_ZERO = "error.validation.negative-or-zero";

    /**
     * The annotation expected the value to be a <b>positive number</b> or <b>duration</b>.
     * <code>0</code> is <b>not</b> allowed.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_POSITIVE = "error.validation.positive";

    /**
     * The annotation expected the value to be a <b>positive number</b> or <b>duration</b>.
     * <code>0</code> is allowed.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_POSITIVE_OR_ZERO = "error.validation.positive-or-zero";

    /**
     * The annotation expected the value to be a <b>number</b> or <b>duration</b>
     * that represents a networking <b>port</b>.
     * A port is a number between <code>1</code> and <code>65535</code>.
     * <code>0</code> is <b>not</b> allowed.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_PORT = "error.validation.invalid-port";

    /**
     * The annotation expected the value to be a <b>character</b> less than another.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>expected</b>: the (included) maximum character allowed.</li>
     * </ul>
     */
    public static final @NotNull String CHARACTER_TOO_BIG = "error.validation.character-too-big";

    /**
     * The annotation expected the value to be a <b>character</b> greater than another.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>expected</b>: the (included) minimum character allowed.</li>
     * </ul>
     */
    public static final @NotNull String CHARACTER_TOO_SMALL = "error.validation.character-too-small";

    /**
     * The annotation expected the value to be a <b>character</b>
     * greater than a character and less than another character.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>min</b>: the (included) minimum character allowed;</li>
     *     <li><b>max</b>: the (included) maximum character allowed.</li>
     * </ul>
     */
    public static final @NotNull String CHARACTER_EXCEEDS_RANGE = "error.validation.character-exceeds-range";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence) with at least one character.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String NOT_EMPTY = "error.validation.not-empty";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * with at least one non-whitespace character.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String NOT_BLANK = "error.validation.not-blank";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * made of only <b>alphabetical</b> characters.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_ALPHABETICAL = "error.validation.invalid-alphabetical";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * made of only <b>alphabetical</b> and <b>digit</b> characters.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_ALPHABETICAL_OR_DIGIT = "error.validation.invalid-alphabetical-or-digit";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that represents an <b>Email</b>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_EMAIL = "error.validation.invalid-email";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that represents a <b>HEX color</b>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_HEX_COLOR = "error.validation.invalid-hex-color";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that represents a <b>hostname</b>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_HOSTNAME = "error.validation.invalid-hostname";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that represents an <b>identifier</b>.
     * An identifier supports the <b>Java variable naming convention</b>
     * (one lowercase alphabetical character or <code>_</code>
     * followed by any number of alphabetical, digit or underscore characters).
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_IDENTIFIER = "error.validation.invalid-identifier";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that represents an <b>IPv4</b>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_IPV4 = "error.validation.invalid-ipv4";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that represents an <b>IPv6</b>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_IPV6 = "error.validation.invalid-ipv6";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that represents an <b>URL</b>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_URL = "error.validation.invalid-url";

    /**
     * The annotation expected the value to be a <b>string</b> (or characters sequence)
     * that matches a certain <b>regular expression</b>.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>expected</b>: the regular expression.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_REGEX = "error.validation.invalid-string";

    /**
     * The annotation expected the value <b>length</b> to be greater than a number and less than another number.
     * The length of the object is calculated using either a <code>length()</code> or <code>size()</code> method
     * (so strings, arrays, collections and maps are allowed).
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value;</li>
     *     <li><b>min</b>: the (included) minimum length allowed;</li>
     *     <li><b>max</b>: the (included) maximum length allowed.</li>
     * </ul>
     */
    public static final @NotNull String EXCEEDS_SIZE = "error.validation.argument-exceeds-size";

    /**
     * The annotation expected the value to be a time <b>before</b> the time of execution.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_BEFORE_NOW = "error.validation.not-before";

    /**
     * The annotation expected the value to be a time <b>before or equal to</b> the time of execution.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_BEFORE_OR_NOW = "error.validation.not-before-or-now";

    /**
     * The annotation expected the value to be a time <b>after</b> the time of execution.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_AFTER_NOW = "error.validation.not-after";

    /**
     * The annotation expected the value to be a time <b>after or equal to</b> the time of execution.
     * <br>
     * Placeholders:
     * <ul>
     *     <li><b>value</b>: the given invalid value.</li>
     * </ul>
     */
    public static final @NotNull String REQUIRED_AFTER_OR_NOW = "error.validation.not-after-or-now";

}
