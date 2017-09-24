package io.saagie.plugin

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.GradleException
import spock.lang.Specification

/**
 * Created by ekoffi on 5/18/17.
 */
class SaagieClientTest extends Specification {


    MockWebServer setupWebserver() {
        MockWebServer mockWebServer = new MockWebServer()
        return mockWebServer
    }

    /**
     * Initialize the client for testing.
     * @param response The response stub which will be returned by mocked api calls.
     * @return The initialized SaagieClient to be tested.
     */
    SaagieClient createSaagieClient() {
        def saagiePluginProperties = Spy(SaagiePluginProperties)
        def saagieClient = Spy(SaagieClient, constructorArgs: [saagiePluginProperties])

        return saagieClient
    }

    def 'Create job with http error'() {
        given:
        def webserver = setupWebserver()
        webserver.enqueue(new MockResponse().setResponseCode(403))
        webserver.start()
        def saagieClient = createSaagieClient()
        saagieClient.configuration.server.url = "http://$webserver.hostName:$webserver.port"

        when:
        saagieClient.createJob('')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job creation(ErrorCode: 403)'

        cleanup:
        webserver.shutdown()
    }

    def 'Create job with success'() {
        given:
        def srv = setupWebserver()
        srv.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id": 1}')
        )
        srv.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$srv.hostName:$srv.port"

        when:
        def jobId = saagieClient.createJob('{"id": 225}')

        then:
        jobId == 1

        cleanup:
        srv.shutdown()
    }
}
