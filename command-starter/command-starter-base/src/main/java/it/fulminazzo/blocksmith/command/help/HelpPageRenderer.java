package it.fulminazzo.blocksmith.command.help;

import org.jetbrains.annotations.NotNull;

public final class HelpPageRenderer {
    private static final int MAX_FONT_WIDTH = 320;

    /**
     * Given the string, returns the index at which it should be <b>truncated</b>
     * so that when rendered it does not exceed {@link #MAX_FONT_WIDTH} pixels.
     *
     * @param string the string
     * @return the index (or {@code -1} if it does not need to be extracted)
     */
    static int getMaxLength(final @NotNull String string) {
        String tmp = "";
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            tmp += c;
            if (MinecraftFontWidth.getWidth(tmp) > MAX_FONT_WIDTH)
                return i - 1;
        }
        return -1;
    }

}
