package it.fulminazzo.blocksmith.util

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Predicate
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class ResourceUtilsTest extends Specification {
    private static final File testResourcesDirectory = new File('src/test/resources')
    private static final File extractTestsDirectory = new File('build/resources/test/resource_utils/extract')
    private static final File extractIfAbsentTestsDirectory = new File('build/resources/test/resource_utils/extract_absent')

    def 'test that getResource with #arguments does not throw'() {
        when:
        def resource = ResourceUtils.getResource(*arguments)

        then:
        resource != null

        and:
        resource.readLines() == ['Hello, world!']

        where:
        arguments << [
                ['test.txt'],
                [ResourceUtils.classLoader, 'test.txt']
        ]
    }

    def 'test that extract with #arguments stores #expected'() {
        given:
        def filename = arguments[0]
        if (filename instanceof ClassLoader) filename = arguments[1]
        if (filename.contains('/')) filename = filename.substring(filename.lastIndexOf('/') + 1)
        def tmp = new File(extractTestsDirectory, filename)
        def lastUpdate = tmp.exists() ? tmp.lastModified() : 0

        when:
        def file = ResourceUtils.extract(*arguments)
        if (file instanceof Path) file = file.toFile()

        then:
        file.exists()

        and:
        file.lastModified() > lastUpdate

        and:
        file.readLines() == expected

        where:
        arguments                                                                      || expected
        // simple
        ['test.txt', extractTestsDirectory]                                            || ['Hello, world!']
        [ResourceUtils.classLoader, 'test.txt', extractTestsDirectory]                 || ['Hello, world!']
        ['test.txt', extractTestsDirectory.toPath()]                                   || ['Hello, world!']
        [ResourceUtils.classLoader, 'test.txt', extractTestsDirectory.toPath()]        || ['Hello, world!']
        // nested
        ['data/schema.sql', extractTestsDirectory]                                     || ['CREATE TABLE secret (id INT);']
        [ResourceUtils.classLoader, 'data/schema.sql', extractTestsDirectory]          || ['CREATE TABLE secret (id INT);']
        ['data/schema.sql', extractTestsDirectory.toPath()]                            || ['CREATE TABLE secret (id INT);']
        [ResourceUtils.classLoader, 'data/schema.sql', extractTestsDirectory.toPath()] || ['CREATE TABLE secret (id INT);']
    }

    def 'test that extractIfAbsent with #arguments stores #expected and does not overwrite'() {
        given:
        extractIfAbsentTestsDirectory.deleteDir()

        when:
        def first = ResourceUtils.extractIfAbsent(*arguments)
        if (first instanceof Path) first = first.toFile()

        then:
        first.exists()

        and:
        first.readLines() == expected

        when:
        def second = ResourceUtils.extractIfAbsent(*arguments)
        if (second instanceof Path) second = second.toFile()

        then:
        second.lastModified() == first.lastModified()

        and:
        second.readLines() == expected

        where:
        arguments                                                                              || expected
        // simple
        ['test.txt', extractIfAbsentTestsDirectory]                                            || ['Hello, world!']
        [ResourceUtils.classLoader, 'test.txt', extractIfAbsentTestsDirectory]                 || ['Hello, world!']
        ['test.txt', extractIfAbsentTestsDirectory.toPath()]                                   || ['Hello, world!']
        [ResourceUtils.classLoader, 'test.txt', extractIfAbsentTestsDirectory.toPath()]        || ['Hello, world!']
        // nested
        ['data/schema.sql', extractIfAbsentTestsDirectory]                                     || ['CREATE TABLE secret (id INT);']
        [ResourceUtils.classLoader, 'data/schema.sql', extractIfAbsentTestsDirectory]          || ['CREATE TABLE secret (id INT);']
        ['data/schema.sql', extractIfAbsentTestsDirectory.toPath()]                            || ['CREATE TABLE secret (id INT);']
        [ResourceUtils.classLoader, 'data/schema.sql', extractIfAbsentTestsDirectory.toPath()] || ['CREATE TABLE secret (id INT);']
    }

    def 'test that #method with invalid resource throws'() {
        when:
        ResourceUtils."$method"(*arguments)

        then:
        def e = thrown(IllegalArgumentException)
        e.message =~ '.*\'not_existing.zip\'.*'

        where:
        method            | arguments
        // getResource
        'getResource'     | ['not_existing.zip']
        'getResource'     | [ResourceUtils.classLoader, 'not_existing.zip']
        // extractIfAbsent
        'extractIfAbsent' | ['not_existing.zip', extractTestsDirectory]
        'extractIfAbsent' | [ResourceUtils.classLoader, 'not_existing.zip', extractTestsDirectory]
        'extractIfAbsent' | ['not_existing.zip', extractTestsDirectory.toPath()]
        'extractIfAbsent' | [ResourceUtils.classLoader, 'not_existing.zip', extractTestsDirectory.toPath()]
        // extract
        'extract'         | ['not_existing.zip', extractTestsDirectory]
        'extract'         | [ResourceUtils.classLoader, 'not_existing.zip', extractTestsDirectory]
        'extract'         | ['not_existing.zip', extractTestsDirectory.toPath()]
        'extract'         | [ResourceUtils.classLoader, 'not_existing.zip', extractTestsDirectory.toPath()]
    }

    def 'test that listClassnames with #arguments loads this class'() {
        when:
        def classes = ResourceUtils.listClassNames(*arguments)

        then:
        classes.contains(ResourceUtilsTest.canonicalName)

        where:
        arguments << [
                [''],
                [ResourceUtilsTest.classLoader, ''],
                ['', (s) -> true],
                [ResourceUtilsTest.classLoader, '', (s) -> true],
                [ResourceUtilsTest.packageName],
                [ResourceUtilsTest.classLoader, ResourceUtilsTest.packageName],
                [ResourceUtilsTest.packageName, (s) -> true],
                [ResourceUtilsTest.classLoader, ResourceUtilsTest.packageName, (s) -> true]
        ]
    }

    def 'test that listResources with #arguments loads our resources from file'() {
        when:
        def resources = ResourceUtils.listResources(*arguments)

        then:
        resources.contains('test.txt')

        where:
        arguments << [
                [''],
                [ResourceUtilsTest.classLoader, ''],
                ['', (s) -> true],
                [ResourceUtilsTest.classLoader, '', (s) -> true]
        ]
    }

    def 'test that loadFromJar with predicate returns #expected'() {
        given:
        def results = []

        and:
        def jar = createTestJar()
        def url = new URI("jar:${jar.toUri()}!/")

        when:
        ResourceUtils.loadFromJar(url.toURL(), results, predicate)

        then:
        results == expected

        where:
        predicate                                           || expected
        (Predicate<String>) ((f) -> true)                   || ['test.txt', 'data/schema.sql']
        (Predicate<String>) ((f) -> f == 'test.txt')        || ['test.txt']
        (Predicate<String>) ((f) -> f == 'data/schema.sql') || ['data/schema.sql']
        (Predicate<String>) ((f) -> false)                  || []
    }

    def 'test that loadFromFileSystem with predicate returns #expected'() {
        given:
        def results = []

        when:
        ResourceUtils.loadFromFileSystem(testResourcesDirectory.toURI().toURL(), results, predicate)

        then:
        results == expected

        where:
        predicate                                           || expected
        (Predicate<String>) ((f) -> true)                   || ['test.txt', 'data/schema.sql']
        (Predicate<String>) ((f) -> f == 'test.txt')        || ['test.txt']
        (Predicate<String>) ((f) -> f == 'data/schema.sql') || ['data/schema.sql']
        (Predicate<String>) ((f) -> false)                  || []
    }

    private Path createTestJar() {
        final Map<String, String> entries = [
                'test.txt'       : 'Hello, world!',
                'data/schema.sql': 'CREATE TABLE secret (id INT);'
        ]
        Path jar = Files.createTempFile('test-resources', '.jar')
        new JarOutputStream(Files.newOutputStream(jar)).withCloseable { o ->
            entries.each { n, c ->
                o.putNextEntry(new JarEntry(n))
                o.write(c.bytes)
                o.closeEntry()
            }
        }
        return jar
    }

}
