package it.fulminazzo.blocksmith.command.help

import spock.lang.Specification

class MinecraftFontWidthTest extends Specification {

    def 'test that getWidth of #text returns #expected'() {
        expect:
        MinecraftFontWidth.getWidth(text) == expected

        where:
        text    || expected
        ''      || 0
        '@'     || 6
        '@@'    || 13
        '@@@'   || 20
        '@@@@@' || 34
        '↑'     || 8
        '↖↖↖↖↖' || 34
    }

}
