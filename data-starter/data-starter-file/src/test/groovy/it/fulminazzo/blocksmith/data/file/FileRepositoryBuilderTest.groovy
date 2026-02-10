package it.fulminazzo.blocksmith.data.file

import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.User
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.util.concurrent.ForkJoinPool

class FileRepositoryBuilderTest extends Specification {

    def 'test build file repository'() {
        when:
        def repository = FileRepository.builder(User)
                .idMapper(User::getId)
                .dataDirectory(new File('build/resources/test/users'))
                .executor(ForkJoinPool.commonPool())
                .dataLanguageFormat(ConfigurationFormat.JSON)
                .logger(LoggerFactory.getLogger(FileRepository.class))
                .build()

        then:
        noExceptionThrown()

        when:
        def user = repository.save(new User(1, 'Alex', 13)).join()

        then:
        user != null

        when:
        def result = repository.findById(user.id).join()

        then:
        result.isPresent()
        result.get() == user

        when:
        repository.delete(user.id).join()

        then:
        !repository.existsById(user.id).join()
    }

}
