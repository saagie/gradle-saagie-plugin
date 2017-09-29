package io.saagie.plugin

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.GradleException
import spock.lang.Specification

/**
 * Tests Saagie client.
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
                .setBody('{"id":135,"capsule_code":"python","current":{"id":234,"number":1,"template":"python {file} impala dn1","file":"python-tests-1.0.0.zip","creation_date":"2017-09-25T16:04:47+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","important":false},"versions":[],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":2,"manual":true,"schedule":"R0\\/2017-05-23T13:59:05.587Z\\/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}')
        )
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobId = saagieClient.createJob('{"platform_id":"2","capsule_code":"python","category":"processing","current":{"options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","file":"59c9291f89ea4/python-tests-1.0.0.zip","template":"python {file} impala dn1"},"description":"","manual":true,"name":"Impala Connection","retry":"","schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S"}')

        then:
        noExceptionThrown()
        jobId == 135
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

        when:
        saagieClient.updateJob(14547, '{"current":{"id":14547,"job_id":6037,"number":1,"template":"python {file} impala dn1 dn2","file":"59c93ba6b29ef/python-tests-1.0.0.zip","creation_date":"2017-09-25T17:15:56+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"email":""}')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job update(ErrorCode: 403)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/14547/version'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update job with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"current":{"id":14547,"job_id":6037,"number":1,"template":"python {file} impala dn1 dn2","file":"59c93ba6b29ef/python-tests-1.0.0.zip","creation_date":"2017-09-25T17:15:56+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"email":""}')
        )
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.updateJob(14547, '{"current":{"id":14547,"job_id":6037,"number":1,"template":"python {file} impala dn1 dn2","file":"59c93ba6b29ef/python-tests-1.0.0.zip","creation_date":"2017-09-25T17:15:56+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"email":""}')

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/14547/version'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'List jobs with wrong response'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse())
        mockWebServer.start()
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobs = saagieClient.getAllJobs()

        then:
        noExceptionThrown()
        jobs.empty

        cleanup:
        mockWebServer.shutdown()
    }
}
