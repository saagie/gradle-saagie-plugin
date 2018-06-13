package io.saagie.plugin.tasks.variables

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logging
import spock.lang.Specification

class UpdateVariableTest extends Specification {
    def logger = Logging.getLogger(this.class)

    def "Empty variables"() {
        given:
        def updateVariable = new UpdateVariable(Spy(SaagiePluginProperties))
        updateVariable.variablesClient = Spy(VariablesClient, constructorArgs: [Spy(SaagiePluginProperties)])

        when:
        updateVariable.updateVariable(logger)

        then:
        0 * updateVariable.variablesClient.createVariable(_ as String)
    }

    def "Update variable"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('[{"id":666,"name":"TEST","value":"value","isPassword":false,"platformId":1}]'))
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
                 id = 666
                 name = 'TEST'
                 value = 'value'
                 password = false
             }]
        }
        def updateVariable = new UpdateVariable(configuration)
        updateVariable.variablesClient = Spy(VariablesClient, constructorArgs: [configuration])

        when:
        updateVariable.updateVariable(logger)

        then:
        1 * updateVariable.variablesClient.updateVariable(666, _ as String)
    }
}
