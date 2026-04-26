package it.fulminazzo.blocksmith.minecraft.util

import org.mockito.Mockito
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpResponse

class APIUtilsTest extends Specification {
    private static final int PORT = 17391

    private static final UUID notchUuid = UUIDUtils.dashed('069a79f444e94726a5befca90e38aaf5')

    def 'test that getSkinData stores skin in cache'() {
        given:
        def uuid = UUIDUtils.dashed('853c80ef3c3749fdaa49938b674adae6')

        when:
        def first = APIUtils.getSkinData(uuid).orElse(null)

        then:
        first != null

        when:
        def cached = APIUtils.SKIN_CACHE[uuid]

        then:
        cached == first

        when:
        def second = APIUtils.getSkinData(uuid).orElse(null)

        then:
        second == cached
        second == first
    }

    def 'test that getSkinData of #uuid returns #found'() {
        when:
        def actual = APIUtils.getSkinData(uuid)

        then:
        actual.isPresent() == found

        where:
        uuid              || found
        notchUuid         || true
        UUID.randomUUID() || false
    }

    def 'test that getSkinData #exception do not throw'() {
        given:
        def mock = Mockito.mockStatic(APIUtils)

        and:
        mock.when { APIUtils.requestBuilder(Mockito.any()) }.thenAnswer {
            throw exception
        }

        when:
        def actual = APIUtils.getSkinData(notchUuid)

        then:
        actual.empty

        cleanup:
        mock.close()

        where:
        exception << [new IOException(), new InterruptedException()]
    }

    def 'test that getUuidFromName stores uuid in cache'() {
        given:
        def name = 'jeb_'

        when:
        def first = APIUtils.getUuidFromName(name).orElse(null)

        then:
        first != null

        when:
        def cached = APIUtils.NAME_CACHE[name]

        then:
        cached == first

        when:
        def second = APIUtils.getUuidFromName(name).orElse(null)

        then:
        second == cached
        second == first
    }

    def 'test that getUuidFromName of #name returns #expected'() {
        when:
        def actual = APIUtils.getUuidFromName(name).orElse(null)

        then:
        actual == expected

        where:
        name                          || expected
        'Notch'                       || notchUuid
        'SomethingSuperLongSoInvalid' || null
    }

    def 'test that getUuidFromName #exception do not throw'() {
        given:
        def mock = Mockito.mockStatic(APIUtils)

        and:
        mock.when { APIUtils.requestBuilder(Mockito.any()) }.thenAnswer {
            throw exception
        }

        when:
        def actual = APIUtils.getUuidFromName('Notch')

        then:
        actual.empty

        cleanup:
        mock.close()

        where:
        exception << [new IOException(), new InterruptedException()]
    }

    def 'test that requestBuilder injects User Agent'() {
        given:
        def catcher = new HttpRequestCatcher().start(PORT)

        and:
        def client = HttpClient.newHttpClient()

        when:
        client.send(
                APIUtils.requestBuilder("http://localhost:$PORT/path").build(),
                HttpResponse.BodyHandlers.ofString()
        )

        then:
        thrown(Exception)

        when:
        def lines = catcher.lines

        then:
        lines.find { it =~ "User-Agent: ${APIUtils.USER_AGENT}" }

        cleanup:
        catcher.stop()
    }

}
