package it.fulminazzo.blocksmith.minecraft.util

import spock.lang.Specification

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HttpRequestCatcherTest extends Specification {

    def 'test that request catcher works'() {
        given:
        def catcher = new HttpRequestCatcher().start()

        when:
        HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create('http://localhost:8080/path')).build(),
                HttpResponse.BodyHandlers.ofString()
        )

        then:
        thrown(Exception)

        when:
        def lines = catcher.lines

        then:
        lines.size() > 0

        and:
        def line = lines[0]
        line == 'GET /path HTTP/1.1'

        cleanup:
        catcher.stop()
    }

}