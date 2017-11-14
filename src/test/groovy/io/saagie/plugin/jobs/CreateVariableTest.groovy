package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logging
import spock.lang.Specification

class CreateVariableTest extends Specification {
    def logger = Logging.getLogger(this.class)

    def "Empty variables"() {
        given:
        def createVariable = new CreateVariable(Spy(SaagiePluginProperties))
        createVariable.saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])

        when:
        createVariable.createVariable(logger)

        then:
        0 * createVariable.saagieClient.createVariable(_ as String)
    }

    def "Create variable"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('{"id":666,"name":"TEST","value":"value","isPassword":false,"platformId":1}'))
        mockWebServer.start()
        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
            platform = 1
        }
        configuration.variables {
            [{
                 name = 'TEST'
                 value = 'value'
                 password = false
             }]
        }
        def createVariable = new CreateVariable(configuration)
        createVariable.saagieClient = Spy(SaagieClient, constructorArgs: [configuration])

        when:
        createVariable.createVariable(logger)

        then:
        1 * createVariable.saagieClient.createVariable(_ as String)
    }
}
