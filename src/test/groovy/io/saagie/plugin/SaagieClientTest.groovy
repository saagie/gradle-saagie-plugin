package io.saagie.plugin

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.GradleException
import spock.lang.Specification

/**
 * Created by ekoffi on 5/18/17.
 */
class SaagieClientTest extends Specification {

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
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse().setResponseCode(403))
        mockWebServer.start()
        def saagieClient = createSaagieClient()
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.createJob('')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job creation(ErrorCode: 403)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Create job with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id": 1}')
        )
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobId = saagieClient.createJob('{"id": 225}')

        then:
        noExceptionThrown()
        jobId == 1
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update job with http error'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse().setResponseCode(403))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'
        saagieClient.configuration.job.id = 25

        when:
        saagieClient.updateJob('{"id" = 25}')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job update(ErrorCode: 403)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/25/version'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update job with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id": 25}')
        )
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'
        saagieClient.configuration.job.id = 25

        when:
        saagieClient.updateJob('{"id": 25}')

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/25/version'

        cleanup:
        mockWebServer.shutdown()
    }
}
