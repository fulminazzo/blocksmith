package it.fulminazzo.blocksmith.config.nightconfig

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.serde.ObjectSerializer
import spock.lang.Specification

class ConfigUtilsTest extends Specification {

    def 'test that setComments correctly sets comments'() {
        given:
        def reference = new MockObject()

        and:
        def expected = CommentedConfig.inMemory()
        expected.set('simple', 'Hello, world!')
        expected.setComment('version', 'This is the first comment')
        expected.set('version', 1.0d)
        expected.setComment('players', 'This comment is multiline\nHope it will work!')
        expected.set('players', 2)
        expected.set('allowed', null)
        expected.set('current', null)

        def authors = expected.createSubConfig()
        authors.set('Alex', 'Fulminazzo')
        authors.set('Camilla', 'Drinkwater')
        expected.set('authors', authors)

        expected.setComment('mentions', 'Special mentions')
        expected.set('mentions', ['Frank'])

        def internal = expected.createSubConfig()
        internal.setComment('java_version', 'This comment is internal')
        internal.set('java_version', 11.0d)

        def gradleVersion = internal.createSubConfig()
        gradleVersion.setComment('gradle', 'Gradle version')
        gradleVersion.set('gradle', 8.14d)
        gradleVersion.setComment('groovy', 'Groovy version')
        gradleVersion.set('groovy', 4.0d)
        internal.set('gradle_version', gradleVersion)

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
        config == expected
    }

}
