package it.fulminazzo.blocksmith.minecraft.util

import org.mockito.Mockito
import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpResponse

class APIUtilsTest extends Specification {
    private static final int PORT = 17391

    def 'test that getUuidFromName of #name returns #expected'() {
        when:
        def actual = APIUtils.getUuidFromName(name).orElse(null)

        then:
        actual == expected

        where:
        name                          || expected
        'Notch'                       || UUIDUtils.dashed('069a79f444e94726a5befca90e38aaf5')
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
