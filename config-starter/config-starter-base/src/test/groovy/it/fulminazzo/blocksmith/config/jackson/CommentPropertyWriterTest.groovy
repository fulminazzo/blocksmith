package it.fulminazzo.blocksmith.config.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.io.SerializedString
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.fulminazzo.blocksmith.config.Comment
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class CommentPropertyWriterTest extends Specification {
    @SuppressWarnings('unused')
    @SuppressFBWarnings('UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD')
    public static String mockField

    def 'test that serializeAsField calls on writeComment'() {
        given:
        def comment = Mock(Comment)

        and:
        def writer = Spy(MockCommentPropertyWrapper, constructorArgs : [Mock(BeanPropertyWriter), comment])
        writer._field = CommentPropertyWriterTest.getDeclaredField('mockField')
        Reflect.on(writer).set('_name', new SerializedString('mockField'))

        and:
        def generator = Mock(JsonGenerator)

        when:
        writer.serializeAsField(new Object(), generator, Mock(SerializerProvider))

        then:
        1 * writer.writeComment(generator, comment)
    }

}
