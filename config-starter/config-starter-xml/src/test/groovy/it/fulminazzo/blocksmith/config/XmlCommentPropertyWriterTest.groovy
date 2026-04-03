package it.fulminazzo.blocksmith.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import spock.lang.Specification

class XmlCommentPropertyWriterTest extends Specification {

    def 'test that writeComment does not throw for non-pretty printer in generator'() {
        given:
        def comment = Mock(Comment)
        comment.value() >> ['Hello', 'world'].toArray()

        and:
        def writer = new XmlConfigurationAdapter.XmlCommentPropertyWriter(
                Mock(BeanPropertyWriter),
                comment
        )

        and:
        def generator = Mock(JsonGenerator)

        when:
        writer.writeComment(generator, comment)

        then:
        noExceptionThrown()

        and:
        1 * generator.writeRaw('<!-- Hello -->')
        1 * generator.writeRaw('<!-- world -->')
    }

}
