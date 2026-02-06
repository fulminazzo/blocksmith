package it.fulminazzo.blocksmith.config.nightconfig

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.serde.ObjectSerializer
import it.fulminazzo.blocksmith.config.Comment
import spock.lang.Specification

class ConfigUtilsTest extends Specification {

    def 'test that fixPropertyNamesAndRemoveNull correctly updates names and overrides nulls'() {
        given:
        def expected = Config.inMemory()
        expected.set('first', 'Hello, world!')
        expected.set('comments_enabled', true)
        def sub = expected.createSubConfig()
        sub.set('java', 11)
        sub.set('gradle_version', 18)
        expected.set('sub', sub)

        and:
        def actual = Config.inMemory()
        actual.set('first', 'Hello, world!')
        actual.set('commentsEnabled', true)
        sub = actual.createSubConfig()
        sub.set('java', 11)
        sub.set('gradleVersion', 18)
        actual.set('sub', sub)

        when:
        ConfigUtils.fixPropertyNames(actual)

        then:
        actual == expected
    }

    def 'test that setComments correctly sets comments'() {
        given:
        def reference = new MockObject()

        and:
        def expected = CommentedConfig.inMemory()
        expected.set('simple', 'Hello, world!')
        expected.setComment('version', ' This is the first comment')
        expected.set('version', 1.0d)
        expected.setComment('players', ' This comment is multiline\n Hope it will work!')
        expected.set('players', 2)
        expected.set('allowed', null)
        expected.set('current', null)

        def authors = expected.createSubConfig()
        authors.set('Alex', 'Fulminazzo')
        authors.set('Camilla', 'Drinkwater')
        expected.set('authors', authors)

        expected.setComment('mentions', ' Special mentions')
        expected.set('mentions', ['Frank'])

        def internal = expected.createSubConfig()
        internal.setComment('java_version', ' This comment is internal')
        internal.set('java_version', 11.0d)

        def gradleVersion = internal.createSubConfig()
        gradleVersion.setComment('gradle', ' Gradle version')
        gradleVersion.set('gradle', 8.14d)
        gradleVersion.setComment('groovy', ' Groovy version')
        gradleVersion.set('groovy', 4.0d)
        internal.set('gradle_version', gradleVersion)

        expected.setComment('internal', ' Internal data, should not be used')
        expected.set('internal', internal)

        and:
        def serializer = ObjectSerializer.standard()
        CommentedConfig config = serializer.serialize(reference, CommentedConfig::inMemory) as CommentedConfig
        CommentedConfig configInternal = config.get('internal')
        configInternal.set('java_version', configInternal.get('javaVersion'))
        configInternal.remove('javaVersion')
        configInternal.set('gradle_version', configInternal.get('gradleVersion'))
        configInternal.remove('gradleVersion')
        reference.current = reference

        when:
        ConfigUtils.setComments(reference, config)

        then:
        checkEquals(config, expected)
    }

    boolean checkEquals(CommentedConfig actual, CommentedConfig expected) {
        def expectedEntries = expected.entrySet()
        def actualEntries = actual.entrySet()

        assert actualEntries.size() == expectedEntries.size()

        for (def entry : expectedEntries) {
            def key = entry.key
            assert actual.getComment(key) == expected.getComment(key)
            def actualValue = actual.get(key)
            def expectedValue = expected.get(key)
            assert actualValue == expectedValue
            if (expectedValue instanceof CommentedConfig)
                checkEquals(actualValue, expectedValue)
        }
        return true
    }

    def 'test that getCommentValue correctly indents comments'() {
        given:
        def comment = Mock(Comment)
        comment.value() >> value

        when:
        def actual = ConfigUtils.getCommentValue(comment)

        then:
        actual == expected

        where:
        value           || expected
        ''              || null
        '    '          || null
        'Hello'         || ' Hello'
        'Hello\nworld!' || ' Hello\n world!'
    }

}
