package io.saagie.plugin.tasks.jobs

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.SaagieClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logging
import spock.lang.Specification

class ListJobsTaskTest extends Specification {
    def logger = Logging.getLogger(this.class)

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
        def listJobs = new ListJobs(configuration)
        listJobs.saagieClient = Spy(SaagieClient, constructorArgs: [configuration])

        when:
        listJobs.listJobs(logger)

        then:
        1 * listJobs.saagieClient.getAllJobs()
    }

    def "Multiples jobs"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('[{"id":135,"capsule_code":"sqoop","current":{"id":234,"number":1,"template":"python {file} impala dn1","creation_date":"2017-09-25T16:04:47+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","important":false},"versions":[],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":2,"manual":true,"schedule":"R0\\\\/2017-05-23T13:59:05.587Z\\\\/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}]'))
        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
            platform = 1
        }
        def listJobs = new ListJobs(configuration)
        listJobs.saagieClient = Spy(SaagieClient, constructorArgs: [configuration])

        when:
        listJobs.listJobs(logger)

        then:
        1 * listJobs.saagieClient.getAllJobs()
    }
    /*   package io.saagie.plugin.tasks.variables

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
       }*/

}
