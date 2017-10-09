package io.saagie.plugin.jobs

import io.saagie.plugin.JobCategory
import io.saagie.plugin.JobType
import io.saagie.plugin.SaagiePluginProperties
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.logging.Logging
import spock.lang.Specification

import java.nio.file.Path

class CreateJobTest extends Specification {
    def logger = Logging.getLogger(this.class)

    def "Empty job"() {
        given:
        def configuration = new SaagiePluginProperties()
        def createJob = new CreateJob(configuration)

        when:
        createJob.createJob(logger)

        then:
        0 * createJob.saagieClient.createJob(_ as String)
    }

    def "Sqoop job processing"() {
        given:
        def configuration = new SaagiePluginProperties()
        configuration.jobs {
            [{
                 type = JobType.SQOOP
                 category = JobCategory.PROCESSING
             }]
        }
        def createJob = new CreateJob(configuration)

        when:
        createJob.createJob(logger)

        then:
        def exception = thrown(UnsupportedOperationException)
        exception.message == 'Can\'t create SQOOP job in processing category.'
    }

    def "Sqoop job"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":135,"capsule_code":"sqoop","current":{"id":234,"number":1,"template":"python {file} impala dn1","creation_date":"2017-09-25T16:04:47+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","important":false},"versions":[],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":2,"manual":true,"schedule":"R0\\/2017-05-23T13:59:05.587Z\\/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}')
        )
        mockWebServer.start()
        def configuration = new SaagiePluginProperties()
        configuration.server {
            url = "http://$mockWebServer.hostName:$mockWebServer.port"
        }
        configuration.jobs {
            [{
                 type = JobType.SQOOP
             }]
        }
        def createJob = new CreateJob(configuration)

        when:
        createJob.createJob(logger)

        then:
        0 * createJob.saagieClient.uploadFile(_ as Path)
    }
}
