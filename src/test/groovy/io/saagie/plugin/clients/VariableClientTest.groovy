package io.saagie.plugin.clients

import io.saagie.plugin.SaagiePluginProperties
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.GradleException
import spock.lang.Specification

/**
 * Tests Variable client.
 * Created by ekoffi on 6/12/18.
 */
class VariableClientTest extends Specification {

    /**
     * Initialize the client for testing.
     * @param response The response stub which will be returned by mocked api calls.
     * @return The initialized VariableClient to be tested.
     */
    VariablesClient createVariableClient() {
        def saagiePluginProperties = Spy(SaagiePluginProperties)
        def variablesClient = Spy(VariablesClient, constructorArgs: [saagiePluginProperties])

        return variablesClient
    }

    def "List vars with empty response"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse())
        mockWebServer.start()
        def variablesClient = Spy(VariablesClient, constructorArgs: [Spy(SaagiePluginProperties)])
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'

        when:
        def jobs = variablesClient.getAllVars()

        then:
        println(jobs)
        noExceptionThrown()
        jobs.empty
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'List vars with correct response'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("[{\"id\":208,\"name\":\"MONGO_PORT\",\"value\":\"27017\",\"isPassword\":false,\"platformId\":12},{\"id\":782,\"name\":\"MYSQL_USER_PASSWORD\",\"isPassword\":true,\"platformId\":12}]"))
        mockWebServer.start()
        def variablesClient = new VariablesClient(Spy(SaagiePluginProperties))
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'

        when:
        def jobs = variablesClient.getAllVars()

        then:
        noExceptionThrown()
        jobs == ['{"id":208,"name":"MONGO_PORT","value":"27017","isPassword":false,"platformId":12}', '{"id":782,"name":"MYSQL_USER_PASSWORD","isPassword":true,"platformId":12}']
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Export var into file'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("[{\"id\":208,\"name\":\"MONGO_PORT\",\"value\":\"27017\",\"isPassword\":false,\"platformId\":12},{\"id\":782,\"name\":\"MYSQL_USER_PASSWORD\",\"isPassword\":true,\"platformId\":12}]"))
        mockWebServer.start()
        def variablesClient = new VariablesClient(Spy(SaagiePluginProperties))
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'
        variablesClient.configuration.packaging.exportFile = 'test'
        variablesClient.configuration.target = './createVars/'

        when:
        variablesClient.exportVariable([208])

        then:
        noExceptionThrown()
        new File('./createVars/test').text == '[{"id":208,"name":"MONGO_PORT","value":"27017","isPassword":false,"platformId":12}]'

        cleanup:
        new File('./createVars/').deleteDir()
        mockWebServer.shutdown()
    }

    def 'Export all vars into file'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("[{\"id\":208,\"name\":\"MONGO_PORT\",\"value\":\"27017\",\"isPassword\":false,\"platformId\":12},{\"id\":782,\"name\":\"MYSQL_USER_PASSWORD\",\"isPassword\":true,\"platformId\":12}]"))
        mockWebServer.start()
        def variablesClient = new VariablesClient(Spy(SaagiePluginProperties))
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'
        variablesClient.configuration.packaging.exportFile = 'test'
        variablesClient.configuration.target = './createAllVars/'

        when:
        variablesClient.exportAllVariables()

        then:
        noExceptionThrown()
        new File('./createAllVars/test').text == '[{"id":208,"name":"MONGO_PORT","value":"27017","isPassword":false,"platformId":12},{"id":782,"name":"MYSQL_USER_PASSWORD","isPassword":true,"platformId":12}]'

        cleanup:
        new File('./createAllVars/').deleteDir()
        mockWebServer.shutdown()
    }

    def 'Create variable with http error'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse().setResponseCode(403))
        mockWebServer.start()
        def variablesClient = createVariableClient()
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'

        when:
        variablesClient.createVariable('')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during variable creation(ErrorCode: 403)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Create variable with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":934,"name":"TEST_VARIABLE","value":"value","isPassword":false,"platformId":1}')
        )
        mockWebServer.start()
        def variablesClient = new VariablesClient(Spy(SaagiePluginProperties))
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'

        when:
        def variableId = variablesClient.createVariable('{"id":934,"name":"TEST_VARIABLE","value":"value","isPassword":false,"platformId":1}')

        then:
        noExceptionThrown()
        variableId == 934
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update variable with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":934,"name":"TEST_VARIABLE","value":"new_value","isPassword":false,"platformId":1}')
        )
        mockWebServer.start()
        def variablesClient = new VariablesClient(Spy(SaagiePluginProperties))
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'

        when:
        variablesClient.updateVariable(934, '{"id":934,"name":"TEST_VARIABLE","value":"new_value","isPassword":false,"platformId":1}')

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars/934'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update variable with http error'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse().setResponseCode(403))
        mockWebServer.start()
        def variablesClient = new VariablesClient(Spy(SaagiePluginProperties))
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'

        when:
        variablesClient.updateVariable(208, '{"id":208,"name":"MONGO_PORT","value":"27017","isPassword":false,"platformId":12}')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during variable update(ErrorCode: 403)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars/208'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Import variable'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('[{"id":934,"name":"TEST_VARIABLE","value":"val","isPassword":false,"platformId":1},{"id":935,"name":"TEST_VARIABLE","value":"val","isPassword":false,"platformId":1}]')
        )
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":208,"name":"MONGO_PORT","value":"27017","isPassword":false,"platformId":12}')
        )
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":934,"name":"TEST_VARIABLE","value":"new_value","isPassword":false,"platformId":1}')
        )
        mockWebServer.start()
        def variablesClient = Spy(VariablesClient, constructorArgs: [Spy(SaagiePluginProperties)])
        variablesClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        variablesClient.configuration.server.platform = '1'
        variablesClient.configuration.packaging.importFile = getClass().classLoader.getResource('variables').file

        when:
        variablesClient.importVariables()

        then:
        noExceptionThrown()
        1 * variablesClient.createVariable('{"id":208,"name":"MONGO_PORT","value":"27017","isPassword":false,"platformId":1}')
        1 * variablesClient.updateVariable(935, '{"id":934,"name":"TEST_VARIABLE","value":"value","isPassword":false,"platformId":1}')
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars'

        cleanup:
        mockWebServer.shutdown()
    }
}
