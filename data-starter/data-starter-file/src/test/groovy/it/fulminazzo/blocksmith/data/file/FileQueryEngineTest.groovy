package it.fulminazzo.blocksmith.data.file

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.User
import spock.lang.Specification

import java.nio.file.Files
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Slf4j
class FileQueryEngineTest extends Specification {
    private static final ConfigurationFormat FORMAT = ConfigurationFormat.JSON
    private static final File WORKING_DIR = new File('build/resources/test/file_query_engine')

    private final ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(log, FORMAT)

    private final ExecutorService executor = Executors.newSingleThreadExecutor()

    private FileQueryEngine<User, Long> engine = new FileQueryEngine<>(adapter, FORMAT, WORKING_DIR, executor)

    void setup() {
        WORKING_DIR.deleteDir()
    }

    void cleanup() {
        if (executor != null) executor.shutdown()
    }

    def 'test that query throws CompletionException on IOException'() {
        when:
        engine.query(a -> {
            throw new IOException('Test exception')
        }).get()

        then:
        def e = thrown(ExecutionException)

        and:
        def cause = e.cause
        (cause instanceof IOException)
        cause.message == 'Test exception'
    }

    def 'test that getFiles only returns data files'() {
        given:
        WORKING_DIR.mkdirs()

        and:
        def expected = (0..9).collect { "tmp${it}.${FORMAT.fileExtension}" }
                .collect { new File(WORKING_DIR, it) }
                .each { Files.createFile(it.toPath()) }

        and:
        (10..20).collect { "tmp${it}.yml" }
                .collect { new File(WORKING_DIR, it) }
                .collect { it.toPath() }
                .collect { Files.createFile(it) }

        when:
        def actual = engine.files

        then:
        actual.sort() == expected.sort()
    }

    def 'test that getDataFile creates directory if not existing'() {
        given:
        WORKING_DIR.deleteDir()

        when:
        engine.getDataFile(1L)

        then:
        WORKING_DIR.exists()
        WORKING_DIR.isDirectory()
    }

}
