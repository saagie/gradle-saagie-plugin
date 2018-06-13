package io.saagie.plugin.tasks.execution

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.ExecutionClient
import io.saagie.plugin.properties.RunningAction
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging
import spock.lang.Specification

class StopJobTaskTest extends Specification {
    def logger = Logging.getLogger(this.class)

    def createConfiguration(MockWebServer mockWebServer) {
        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
            platform = 1
        }
        configuration.jobs {
            [{
                 id = 666
             }]
        }
        return configuration
    }

    def "Empty job list"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
            platform = 1
        }
        def stopJob = new StopJob(configuration)

        when:
        stopJob.stopJob(logger)

        then:
        0 * stopJob.executionClient.jobManagement(_ as Integer, _ as RunningAction)
    }

    def "Stop non existing job"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500))

        def configuration = createConfiguration(mockWebServer)
        def stopJob = new StopJob(configuration)
        stopJob.executionClient = Spy(ExecutionClient, constructorArgs: [configuration])

        when:
        stopJob.stopJob(logger)

        then:
        1 * stopJob.executionClient.jobManagement(666, RunningAction.stop)
        def exception = thrown(GradleException)
        exception.message == 'Error during job stop (ErrorCode: 500)'
    }

    def "Stop job"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204))

        def configuration = createConfiguration(mockWebServer)
        def stopJob = new StopJob(configuration)
        stopJob.executionClient = Spy(ExecutionClient, constructorArgs: [configuration])

        when:
        stopJob.stopJob(logger)

        then:
        noExceptionThrown()
        1 * stopJob.executionClient.jobManagement(666, RunningAction.stop)
    }
}
