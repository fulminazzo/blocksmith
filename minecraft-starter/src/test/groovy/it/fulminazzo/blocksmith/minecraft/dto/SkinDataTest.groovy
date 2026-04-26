package it.fulminazzo.blocksmith.minecraft.dto

import com.google.gson.Gson
import it.fulminazzo.blocksmith.minecraft.util.UUIDUtils
import spock.lang.Specification

class SkinDataTest extends Specification {

    def 'test that SkinData conversion works'() {
        given:
        def skinData = new SkinData(
                UUID.randomUUID(),
                'name',
                new SkinData.Skin(
                        'skin',
                        slim
                ),
                cape,
                signature
        )

        when:
        def base64 = skinData.toBase64()

        then:
        noExceptionThrown()

        when:
        def actual = SkinData.fromBase64(base64, signature)

        then:
        actual == skinData

        where:
        slim  | cape   | signature
        false | null   | null
        false | null   | 'signature'
        false | 'cape' | null
        false | 'cape' | 'signature'
        true  | null   | null
        true  | null   | 'signature'
        true  | 'cape' | null
        true  | 'cape' | 'signature'
    }

    def 'test that fromBase64 checks for slim model'() {
        given:
        def expected = new SkinData(
                UUID.randomUUID(),
                'name',
                new SkinData.Skin(
                        'skin',
                        false
                ),
                null,
                null
        )

        and:
        def mappings = [
                'profileId'        : UUIDUtils.undashed(expected.uuid),
                'profileName'      : expected.name,
                'signatureRequired': false,
                'textures'         : [
                        'SKIN': [
                                'url'     : expected.skin.url,
                                'metadata': metadata
                        ]
                ]
        ]

        when:
        def actual = SkinData.fromBase64(
                Base64.encoder.encodeToString(new Gson().toJson(mappings).bytes),
                null
        )

        then:
        actual == expected

        where:
        metadata << [
                ['model': 'fat'],
                ['invalid': 'skin']
        ]
    }

}
