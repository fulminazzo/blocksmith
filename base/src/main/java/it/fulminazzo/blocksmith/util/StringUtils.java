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

        String current = "";
        char[] chars = string.toCharArray();
        main:
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            for (String r : quotes) {
                Pattern p = quotePatterns.get(r);
                if (p.matcher(current + c).find()) {
                    String start = current;
                    for (; i < chars.length; i++) {
                        start += chars[i];
                        if (!start.matches(current + r + "$")) {
                            start = start.substring(0, start.length() - 1);
                            break;
                        }
                    }
                    start = start.substring(current.length());
                    String tmp = start;
                    for (; i < chars.length; i++) {
                        c = chars[i];
                        if (c == '\\') {
                            if (i < chars.length - 1) tmp += chars[++i];
                            continue;
                        }
                        tmp += c;
                        if (tmp.endsWith(start)) {
                            if (!quoted) tmp = tmp.substring(start.length(), tmp.length() - start.length());
                            break;
                        }
                    }
                    current += tmp;
                    continue main;
                }
            }

            if (pattern.matcher(current + c).find()) {
                String tmp = current;
                for (; i < chars.length; i++) {
                    tmp += chars[i];
                    if (!tmp.matches(current + regex + "$")) {
                        i--;
                        break;
                    }
                }
                strings.add(current);
                current = "";
                continue;
            }

            current += c;
        }
        strings.add(current);
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
    public static @NotNull List<String> split(final @Nullable String string,
                                              final @NotNull String regex,
                                              final @NotNull Pair<String, String> @NotNull ... parenthesis) {
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

        String current = "";
        char[] chars = string.toCharArray();
        main:
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            for (Pair<String, String> p : parenthesis) {
                Pattern openPattern = parenthesisPatterns.get(p).getFirst();
                Pattern closePattern = parenthesisPatterns.get(p).getSecond();
                if (openPattern.matcher(current + c).find()) {
                    int depth = 1;
                    current += c;
                    for (++i; i < chars.length; i++) {
                        c = chars[i];
                        current += c;
                        if (openPattern.matcher(current).find()) depth++;
                        else if (closePattern.matcher(current).find()) depth--;
                        if (depth == 0) break;
                    }
                    continue main;
                }
            }

            if (pattern.matcher(current + c).find()) {
                String tmp = current;
                for (; i < chars.length; i++) {
                    tmp += chars[i];
                    if (!tmp.matches(Pattern.quote(current) + regex + "$")) {
                        i--;
                        break;
                    }
                }
                strings.add(current);
                current = "";
                continue;
            }

            current += c;
        }
        strings.add(current);
        return List.copyOf(strings);
    }

}
