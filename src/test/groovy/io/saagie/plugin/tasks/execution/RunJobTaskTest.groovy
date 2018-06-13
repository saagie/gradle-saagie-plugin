package io.saagie.plugin.tasks.execution

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.ExecutionClient
import io.saagie.plugin.properties.RunningAction
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging
import spock.lang.Specification

class RunJobTaskTest extends Specification {
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
        mockWebServer.start()

        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
            platform = 1
        }
        def runJob = new RunJob(configuration)
        when:
        runJob.runJob(logger)

        then:
        0 * runJob.executionClient.jobManagement(_ as Integer, _ as RunningAction)
    }

    def "Run non existing job"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500))
        mockWebServer.start()

        def configuration = createConfiguration(mockWebServer)
        def runJob = new RunJob(configuration)
        runJob.executionClient = Spy(ExecutionClient, constructorArgs: [configuration])

        when:
        runJob.runJob(logger)

        then:
        1 * runJob.executionClient.jobManagement(666, RunningAction.run)
        def exception = thrown(GradleException)
        exception.message == 'Error during job run (ErrorCode: 500)'
    }

    def "Run already running job"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(412))
        mockWebServer.start()

        def configuration = createConfiguration(mockWebServer)
        def runJob = new RunJob(configuration)
        runJob.executionClient = Spy(ExecutionClient, constructorArgs: [configuration])

        when:
        runJob.runJob(logger)

        then:
        1 * runJob.executionClient.jobManagement(666, RunningAction.run)
        def exception = thrown(GradleException)
        exception.message == 'Error during job run (ErrorCode: 412)'
    }

    def "Run job"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204))
        mockWebServer.start()

        def configuration = createConfiguration(mockWebServer)
        def runJob = new RunJob(configuration)
        runJob.executionClient = Spy(ExecutionClient, constructorArgs: [configuration])

        when:
        runJob.runJob(logger)

        then:
        noExceptionThrown()
        1 * runJob.executionClient.jobManagement(666, RunningAction.run)
    }
}
