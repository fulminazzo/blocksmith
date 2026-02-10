package it.fulminazzo.blocksmith.data.file

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.Repository
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.function.ConsumerException
import it.fulminazzo.blocksmith.function.FunctionException
import org.jetbrains.annotations.NotNull

import java.nio.file.Files
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Slf4j
class FileRepositoryTest extends RepositoryTest {
    private static final ConfigurationFormat FORMAT = ConfigurationFormat.JSON

    private final File workingDir = new File('build/resources/test/file_repository')

    private final ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(log, FORMAT)

    private final ExecutorService executor = Executors.newSingleThreadExecutor()

    void setup() {
        setupRepository()
    }

    void cleanup() {
        executor.shutdown()
    }

    def 'test executeOnMany throws CompletionException'() {
        given:
        def ids = [1L, 2L]

        when:
        repository.executeOnMany(ids, function).join()

        then:
        def e = thrown(CompletionException)
        (e.cause instanceof IOException)

        where:
        function << [
                (ConsumerException<File, IOException>) (f -> {
                    throw new IOException('test exception')
                }),
                (FunctionException<File, User, IOException>) (f -> {
                    throw new IOException('test exception')
                })
        ]
    }

    def 'test executeOnSingle throws CompletionException'() {
        given:
        def id = 1L

        when:
        repository.executeOnSingle(id, function).join()

        then:
        def e = thrown(CompletionException)
        (e.cause instanceof IOException)

        where:
        function << [
                (ConsumerException<File, IOException>) (f -> {
                    throw new IOException('test exception')
                }),
                (FunctionException<File, User, IOException>) (f -> {
                    throw new IOException('test exception')
                })
        ]
    }

    def 'test that getDataFile creates directory if not existing'() {
        given:
        Files.deleteIfExists(workingDir.toPath())

        when:
        repository.getDataFile(1L)

        then:
        workingDir.exists()
        workingDir.isDirectory()
    }

    @Override
    Repository<User, Long> initializeRepository() {
        return new FileRepository<>(
                workingDir,
                User,
                User::getId,
                executor,
                log,
                FORMAT
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return FORMAT.getFile(workingDir, id.toString()).exists()
    }

    @Override
    void insert(final @NotNull User data) {
        adapter.store(workingDir, data.id.toString(), data)
    }

}
