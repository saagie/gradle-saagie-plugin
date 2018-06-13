package io.saagie.plugin.tasks.variables

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logging
import spock.lang.Specification

class ListVarsTaskTest extends Specification {
    def logger = Logging.getLogger(this.class)

    def "Empty variables"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
            platform = 1
        }
        def listVars = new ListVars(configuration)
        listVars.variablesClient = Spy(VariablesClient, constructorArgs: [configuration])

        when:
        listVars.listVars(logger)

        then:
        1 * listVars.variablesClient.getAllVars()
    }

    def "Multiples variables"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('[{"id":208,"name":"MONGO_PORT","value":"27017","isPassword":false,"platformId":12},{"id":782,"name":"MYSQL_USER_PASSWORD","isPassword":true,"platformId":12},{"id":934,"name":"TEST_VARIABLE","value":"value","isPassword":false,"platformId":12}]'))
        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
            platform = 1
        }
        def listVars = new ListVars(configuration)
        listVars.variablesClient = Spy(VariablesClient, constructorArgs: [configuration])

        when:
        listVars.listVars(logger)

        then:
        1 * listVars.variablesClient.getAllVars()
    }
}
