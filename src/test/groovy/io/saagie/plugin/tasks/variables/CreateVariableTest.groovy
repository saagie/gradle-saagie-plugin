package io.saagie.plugin.tasks.variables

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logging
import spock.lang.Specification

class CreateVariableTest extends Specification {
    def logger = Logging.getLogger(this.class)

    def "Empty variables"() {
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
        def createVariable = new CreateVariable(configuration)
        createVariable.variablesClient = Spy(VariablesClient, constructorArgs: [configuration])

        when:
        createVariable.createVariable(logger)

        then:
        0 * createVariable.variablesClient.createVariable(_ as String)
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
        createVariable.variablesClient = Spy(VariablesClient, constructorArgs: [configuration])

        when:
        createVariable.createVariable(logger)

        then:
        1 * createVariable.variablesClient.createVariable(_ as String)
    }
}
