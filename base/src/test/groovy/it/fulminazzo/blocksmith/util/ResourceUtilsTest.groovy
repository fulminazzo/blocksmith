package it.fulminazzo.blocksmith.util

import spock.lang.Specification

import java.nio.file.Path

class ResourceUtilsTest extends Specification {
    private static final File extractTestsDirectory = new File('build/resources/test/resource_utils/extract')

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
        ['test.txt', extractTestsDirectory]                                            || ['Hello, world!']
        [ResourceUtils.classLoader, 'test.txt', extractTestsDirectory]                 || ['Hello, world!']
        ['test.txt', extractTestsDirectory.toPath()]                                   || ['Hello, world!']
        [ResourceUtils.classLoader, 'test.txt', extractTestsDirectory.toPath()]        || ['Hello, world!']
        ['data/schema.sql', extractTestsDirectory]                                     || ['CREATE TABLE secret (id INT);']
        [ResourceUtils.classLoader, 'data/schema.sql', extractTestsDirectory]          || ['CREATE TABLE secret (id INT);']
        ['data/schema.sql', extractTestsDirectory.toPath()]                            || ['CREATE TABLE secret (id INT);']
        [ResourceUtils.classLoader, 'data/schema.sql', extractTestsDirectory.toPath()] || ['CREATE TABLE secret (id INT);']
    }

}
