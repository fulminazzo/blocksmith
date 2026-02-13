package it.fulminazzo.blocksmith.data.file

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import org.jetbrains.annotations.NotNull

import java.nio.file.Files
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Slf4j
class FileRepositoryTest extends RepositoryTest<FileRepository<User, Long>> {
    private static final ConfigurationFormat FORMAT = ConfigurationFormat.JSON
    private static final File WORKING_DIR = new File('build/resources/test/file_repository')

    private final ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(log, FORMAT)

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    void setup() {
        WORKING_DIR.deleteDir()
        WORKING_DIR.mkdirs()
        new File(WORKING_DIR, 'tmp').createNewFile()

        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        executor.shutdown()
    }

    @Override
    FileRepository<User, Long> initializeRepository() {
        return new FileRepository<>(
                new FileQueryEngine<>(
                        adapter,
                        FORMAT,
                        WORKING_DIR,
                        executor
                ),
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return FORMAT.getFile(WORKING_DIR, id.toString()).exists()
    }

    @Override
    void insert(final @NotNull User entity) {
        adapter.store(WORKING_DIR, entity.id.toString(), entity)
    }

    @Override
    void remove(final @NotNull Long id) {
        Files.deleteIfExists(FORMAT.getFile(WORKING_DIR, id.toString()).toPath())
    }

}
