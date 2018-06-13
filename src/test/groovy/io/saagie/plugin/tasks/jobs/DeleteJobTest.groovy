package io.saagie.plugin.tasks.jobs

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.SaagieClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logging
import spock.lang.Specification

class DeleteJobTest extends Specification {
    def logger = Logging.getLogger(this.class)

    def "Empty job"() {
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
        def deleteJob = new DeleteJob(configuration)

        when:
        deleteJob.deleteJob(logger)

        then:
        0 * deleteJob.saagieClient.deleteJob(_ as Integer)
    }

    def "delete job"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204))
        mockWebServer.start()
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
        def deleteJob = new DeleteJob(configuration)
        deleteJob.saagieClient = Spy(SaagieClient, constructorArgs: [configuration])

        when:
        deleteJob.deleteJob(logger)

        then:
        1 * deleteJob.saagieClient.deleteJob(666)
    }
}
