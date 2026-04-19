package it.fulminazzo.blocksmith.util;

import it.fulminazzo.blocksmith.structure.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A collection of utilities to work with strings.
 */
public final class StringUtils {

    /**
     * Divides the given string into multiple substrings, based on the provided regular expression.
     *
     * @param string the string
     * @param regex  the expression to use for splitting
     * @param quotes if any of these "quotes" are met during splitting,
     *               if the expression is found before the same quote is met,
     *               the string will not be split (useful for splitting quoted arguments).
     *               Supports regular expressions
     * @return the strings
     */
    public static @NotNull List<String> split(final @Nullable String string,
                                              final @NotNull String regex,
                                              final String @NotNull ... quotes) {
        return split(string, regex, true, quotes);
    }

    /**
     * Divides the given string into multiple substrings, based on the provided regular expression.
     *
     * @param string the string
     * @param regex  the expression to use for splitting
     * @param quoted if <code>true</code>, the found quotes will be prepended and appended to the results (if present)
     * @param quotes if any of these "quotes" are met during splitting,
     *               if the expression is found before the same quote is met,
     *               the string will not be split (useful for splitting quoted arguments).
     *               Supports regular expressions
     * @return the strings
     */
    public static @NotNull List<String> split(final @Nullable String string,
                                              final @NotNull String regex,
                                              final boolean quoted,
                                              final String @NotNull ... quotes) {
        if (string == null) return Collections.emptyList();
        if (string.isEmpty()) return Collections.singletonList("");
        final List<String> strings = new LinkedList<>();
        final Pattern pattern = Pattern.compile(regex + "$");
        final Map<String, Pattern> quotePatterns = Arrays.stream(quotes)
                .collect(Collectors.toMap(r -> r, r -> Pattern.compile(r + "$")));

        StringBuilder current = new StringBuilder();
        char[] chars = string.toCharArray();
        main:
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            for (String r : quotes) {
                Pattern p = quotePatterns.get(r);
                if (p.matcher(current.toString() + c).find()) {
                    StringBuilder startBuilder = new StringBuilder(current);
                    for (; i < chars.length; i++) {
                        startBuilder.append(chars[i]);
                        if (!Pattern.matches(current + r + "$", startBuilder)) {
                            startBuilder.setLength(startBuilder.length() - 1);
                            break;
                        }
                    }
                    String start = startBuilder.substring(current.length());
                    StringBuilder tmpBuilder = new StringBuilder(start);
                    for (; i < chars.length; i++) {
                        c = chars[i];
                        if (c == '\\') {
                            if (i < chars.length - 1) tmpBuilder.append(chars[++i]);
                            continue;
                        }
                        tmpBuilder.append(c);
                        if (tmpBuilder.toString().endsWith(start)) {
                            if (!quoted) {
                                tmpBuilder.delete(0, start.length());
                                tmpBuilder.setLength(tmpBuilder.length() - start.length());
                            }
                            break;
                        }
                    }
                    current.append(tmpBuilder);
                    continue main;
                }
            }

            if (pattern.matcher(current.toString() + c).find()) {
                StringBuilder tmpBuilder = new StringBuilder(current);
                for (; i < chars.length; i++) {
                    tmpBuilder.append(chars[i]);
                    if (!Pattern.matches(current + regex + "$", tmpBuilder)) {
                        i--;
                        break;
                    }
                }
                strings.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }
        strings.add(current.toString());
        return List.copyOf(strings);
    }

    /**
     * Divides the given string into multiple substrings, based on the provided regular expression.
     *
     * @param string      the string
     * @param regex       the expression to use for splitting
     * @param parenthesis if any of these "parenthesis" are met during splitting,
     *                    if the expression is found before the same quote is met,
     *                    the string will not be split (useful for splitting parenthesized arguments).
     *                    Supports regular expressions
     * @return the strings
     */
    @SafeVarargs
    public static @NotNull List<String> split(final @Nullable String string,
                                              final @NotNull String regex,
                                              final @NotNull Pair<@NotNull String, @NotNull String> @NotNull ... parenthesis) {
        if (string == null) return Collections.emptyList();
        if (string.isEmpty()) return Collections.singletonList("");
        final List<String> strings = new LinkedList<>();
        final Pattern pattern = Pattern.compile(regex + "$");
        final Map<Pair<String, String>, Pair<Pattern, Pattern>> parenthesisPatterns = Arrays.stream(parenthesis)
                .collect(Collectors.toMap(
                        p -> p,
                        p -> Pair.of(
                                Pattern.compile(Pattern.quote(p.getFirst()) + "$"),
                                Pattern.compile(Pattern.quote(p.getSecond()) + "$")
                        )
                ));

        StringBuilder current = new StringBuilder();
        char[] chars = string.toCharArray();
        main:
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            for (Pair<String, String> p : parenthesis) {
                Pattern openPattern = parenthesisPatterns.get(p).getFirst();
                Pattern closePattern = parenthesisPatterns.get(p).getSecond();
                if (openPattern.matcher(current.toString() + c).find()) {
                    int depth = 1;
                    current.append(c);
                    for (++i; i < chars.length; i++) {
                        c = chars[i];
                        current.append(c);
                        if (openPattern.matcher(current).find()) depth++;
                        else if (closePattern.matcher(current).find()) depth--;
                        if (depth == 0) break;
                    }
                    continue main;
                }
            }

            if (pattern.matcher(current.toString() + c).find()) {
                StringBuilder tmpBuilder = new StringBuilder(current);
                for (; i < chars.length; i++) {
                    tmpBuilder.append(chars[i]);
                    if (!Pattern.matches(Pattern.quote(current.toString()) + regex + "$", tmpBuilder)) {
                        i--;
                        break;
                    }
                }
                strings.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }
        strings.add(current.toString());
        return List.copyOf(strings);
    }

}
