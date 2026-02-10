package it.fulminazzo.blocksmith.data.file

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.Repository
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.function.BiConsumerException
import it.fulminazzo.blocksmith.function.BiFunctionException
import it.fulminazzo.blocksmith.function.ConsumerException
import it.fulminazzo.blocksmith.function.FunctionException
import org.jetbrains.annotations.NotNull

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Slf4j
class FileRepositoryTest extends RepositoryTest {
    private static final ConfigurationFormat FORMAT = ConfigurationFormat.JSON
    private static final File WORKING_DIR = new File('build/resources/test/file_repository')

    private final ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(log, FORMAT)

    private final ExecutorService executor = Executors.newSingleThreadExecutor()

    void setup() {
        WORKING_DIR.deleteDir()

        setupRepository()
    }

    void cleanup() {
        executor.shutdown()
    }

    def 'test count returns empty on non-existing directory'() {
        given:
        if (WORKING_DIR.exists()) WORKING_DIR.deleteDir()

        when:
        def actual = repository.count().join()

        then:
        actual == 0L
    }

    /*
     * MULTIPLE
     */

    def 'test executeOnMany(Consumer) returns all files'() {
        given:
        def actual = []
        def expected = [
                FORMAT.getFile(WORKING_DIR, FIRST.id.toString()),
                FORMAT.getFile(WORKING_DIR, SECOND.id.toString())
        ]

        when:
        repository.executeOnMany((ConsumerException<File, IOException>) (f -> {
            actual.add(f)
        })).join()

        then:
        actual.sort() == expected.sort()
    }

    def 'test executeOnMany(Consumer) returns empty if not existing directory'() {
        given:
        def actual = []
        def expected = []

        and:
        if (WORKING_DIR.exists()) WORKING_DIR.deleteDir()

        when:
        repository.executeOnMany((ConsumerException<File, IOException>) (f -> {
            actual.add(f)
        })).join()

        then:
        actual == expected
    }

    def 'test executeOnMany(Function) returns all files'() {
        given:
        def expected = [
                FORMAT.getFile(WORKING_DIR, FIRST.id.toString()),
                FORMAT.getFile(WORKING_DIR, SECOND.id.toString())
        ]

        when:
        def actual = repository.executeOnMany((FunctionException<File, File, IOException>) (f -> f)).join()

        then:
        actual.sort() == expected.sort()
    }

    def 'test executeOnMany(Function) returns empty if not existing directory'() {
        given:
        def expected = []

        and:
        if (WORKING_DIR.exists()) WORKING_DIR.deleteDir()

        when:
        def actual = repository.executeOnMany((FunctionException<File, File, IOException>) (f -> f)).join()

        then:
        actual == expected
    }

    /*
     * MULTIPLE FILTERED
     */

    def 'test executeOnManyData(Collection, Consumer) returns only filtered files'() {
        given:
        def actual = []

        when:
        repository.executeOnManyData(entries, (ConsumerException<File, IOException>) (f -> {
            actual.add(f)
        })).join()

        then:
        actual.sort() == expected.sort()

        where:
        entries  || expected
        [FIRST]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    def 'test executeOnManyData(Collection, BiConsumer) returns only filtered files'() {
        given:
        def actualEntries = []
        def actual = []

        when:
        repository.executeOnManyData(entries, (BiConsumerException<File, User, IOException>) ((f, u) -> {
            actualEntries.add(u)
            actual.add(f)
        })).join()

        then:
        actual.sort() == expected.sort()
        actualEntries.sort() == entries.sort()

        where:
        entries  || expected
        [FIRST]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    def 'test executeOnManyData(Collection, Function) returns only filtered files'() {
        when:
        def actual = repository.executeOnManyData(entries,
                (FunctionException<File, File, IOException>) (f -> f)
        ).join()

        then:
        actual.sort() == expected.sort()

        where:
        entries  || expected
        [FIRST]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    def 'test executeOnManyData(Collection, Function) returns only filtered files'() {
        given:
        def actualEntries = []

        when:
        def actual = repository.executeOnManyData(entries,
                (BiFunctionException<File, User, File, IOException>) ((f, u) -> {
                    actualEntries.add(u)
                    return f
                })
        ).join()

        then:
        actual.sort() == expected.sort()
        actualEntries.sort() == entries.sort()

        where:
        entries  || expected
        [FIRST]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    def 'test executeOnMany(Collection, Consumer) returns only filtered files'() {
        given:
        def actual = []

        when:
        repository.executeOnMany(entries, (ConsumerException<File, IOException>) (f -> {
            actual.add(f)
        })).join()

        then:
        actual.sort() == expected.sort()

        where:
        entries     || expected
        [FIRST.id]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND.id] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    def 'test executeOnMany(Collection, BiConsumer) returns only filtered files'() {
        given:
        def actualEntries = []
        def actual = []

        when:
        repository.executeOnMany(entries, (BiConsumerException<File, Long, IOException>) ((f, l) -> {
            actualEntries.add(l)
            actual.add(f)
        })).join()

        then:
        actual.sort() == expected.sort()
        actualEntries.sort() == entries.sort()

        where:
        entries     || expected
        [FIRST.id]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND.id] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    def 'test executeOnMany(Collection, Function) returns only filtered files'() {
        when:
        def actual = repository.executeOnMany(entries,
                (FunctionException<File, File, IOException>) (f -> f)
        ).join()

        then:
        actual.sort() == expected.sort()

        where:
        entries     || expected
        [FIRST.id]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND.id] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    def 'test executeOnMany(Collection, Function) returns only filtered files'() {
        given:
        def actualEntries = []

        when:
        def actual = repository.executeOnMany(entries,
                (BiFunctionException<File, Long, File, IOException>) ((f, l) -> {
                    actualEntries.add(l)
                    return f
                })
        ).join()

        then:
        actual.sort() == expected.sort()
        actualEntries.sort() == entries.sort()

        where:
        entries     || expected
        [FIRST.id]  || [FORMAT.getFile(WORKING_DIR, FIRST.id.toString())]
        [SECOND.id] || [FORMAT.getFile(WORKING_DIR, SECOND.id.toString())]
    }

    /*
     * SINGLE
     */

    def 'test that executeOnSingleData(T, Consumer) returns correct file'() {
        given:
        def expected = FORMAT.getFile(WORKING_DIR, data.id.toString())
        def actual = null

        when:
        repository.executeOnSingleData(data, (ConsumerException<File, IOException>) (f -> {
            actual = f
        })).join()

        then:
        actual == expected

        where:
        data << [FIRST, SECOND]
    }

    def 'test that executeOnSingleData(T, Function) returns correct file'() {
        given:
        def expected = FORMAT.getFile(WORKING_DIR, data.id.toString())

        when:
        def actual = repository.executeOnSingleData(data,
                (FunctionException<File, File, IOException>) (f -> {
                    return f
                })
        ).join()

        then:
        actual == expected

        where:
        data << [FIRST, SECOND]
    }

    def 'test that executeOnSingle(ID, Consumer) returns correct file'() {
        given:
        def expected = FORMAT.getFile(WORKING_DIR, id.toString())
        def actual = null

        when:
        repository.executeOnSingle(id, (ConsumerException<File, IOException>) (f -> {
            actual = f
        })).join()

        then:
        actual == expected

        where:
        id << [FIRST, SECOND].collect { it.id }
    }

    def 'test that executeOnSingle(ID, Function) returns correct file'() {
        given:
        def expected = FORMAT.getFile(WORKING_DIR, id.toString())

        when:
        def actual = repository.executeOnSingle(id,
                (FunctionException<File, File, IOException>) (f -> {
                    return f
                })
        ).join()

        then:
        actual == expected

        where:
        id << [FIRST, SECOND].collect { it.id }
    }

    def 'test that getDataFile creates directory if not existing'() {
        when:
        repository.getDataFile(1L)

        then:
        WORKING_DIR.exists()
        WORKING_DIR.isDirectory()
    }

    @Override
    Repository<User, Long> initializeRepository() {
        return new FileRepository<>(
                WORKING_DIR,
                User,
                User::getId,
                executor,
                log,
                FORMAT
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return FORMAT.getFile(WORKING_DIR, id.toString()).exists()
    }

    @Override
    void insert(final @NotNull User data) {
        adapter.store(WORKING_DIR, data.id.toString(), data)
    }

}
