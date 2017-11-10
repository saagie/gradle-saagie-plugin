package io.saagie.plugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.gradle.api.GradleException
import spock.lang.Specification

import java.nio.file.Paths
import java.util.zip.ZipFile

/**
 * Tests Saagie client.
 * Created by ekoffi on 5/18/17.
 */
class SaagieClientTest extends Specification {

    /**
     * Initialize the client for testing.
     * @param response The response stub which will be returned by mocked api calls.
     * @return The initialized SaagieClient to be tested.
     */
    SaagieClient createSaagieClient() {
        def saagiePluginProperties = Spy(SaagiePluginProperties)
        def saagieClient = Spy(SaagieClient, constructorArgs: [saagiePluginProperties])

        return saagieClient
    }

    def 'Create job with http error'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse().setResponseCode(403))
        mockWebServer.start()
        def saagieClient = createSaagieClient()
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.createJob('')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job creation(ErrorCode: 403)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Create job with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":135,"capsule_code":"python","current":{"id":234,"number":1,"template":"python {file} impala dn1","file":"python-tests-1.0.0.zip","creation_date":"2017-09-25T16:04:47+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","important":false},"versions":[],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":2,"manual":true,"schedule":"R0\\/2017-05-23T13:59:05.587Z\\/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}')
        )
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobId = saagieClient.createJob('{"platform_id":"2","capsule_code":"python","category":"processing","current":{"options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","file":"59c9291f89ea4/python-tests-1.0.0.zip","template":"python {file} impala dn1"},"description":"","manual":true,"name":"Impala Connection","retry":"","schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S"}')

        then:
        noExceptionThrown()
        jobId == 135
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update job with http error'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse().setResponseCode(403))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.updateJob(14547, '{"current":{"id":14547,"job_id":6037,"number":1,"template":"python {file} impala dn1 dn2","file":"59c93ba6b29ef/python-tests-1.0.0.zip","creation_date":"2017-09-25T17:15:56+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"email":""}')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job update(ErrorCode: 403)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/14547/version'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update job with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"current":{"id":14547,"job_id":6037,"number":1,"template":"python {file} impala dn1 dn2","file":"59c93ba6b29ef/python-tests-1.0.0.zip","creation_date":"2017-09-25T17:15:56+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"email":""}')
        )
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.updateJob(14547, '{"current":{"id":14547,"job_id":6037,"number":1,"template":"python {file} impala dn1 dn2","file":"59c93ba6b29ef/python-tests-1.0.0.zip","creation_date":"2017-09-25T17:15:56+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"email":""}')

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/14547/version'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'List jobs with empty response'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse())
        mockWebServer.start()
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobs = saagieClient.getAllJobs()

        then:
        noExceptionThrown()
        jobs.empty
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'List jobs with correct response'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody("[{\"id\":6109,\"capsule_code\":\"docker\",\"current\":{\"id\":14673,\"job_id\":6109,\"number\":1,\"packageUrl\":\"saagie.dk\\/saagie-datagov:1.0.1\",\"creation_date\":\"2017-09-27T14:29:35+00:00\",\"options\":[],\"url\":\"5-6109-saagiedatagovernance.prod.saagie.io\",\"cpu\":0.6,\"memory\":1024,\"disk\":1024,\"port\":8080,\"enableAuth\":true,\"authUsername\":\"saagie\"},\"versions\":[{\"id\":14673,\"job_id\":6109,\"number\":1,\"packageUrl\":\"saagie.dk\\/saagie-datagov:1.0.1\",\"creation_date\":\"2017-09-27T14:29:35+00:00\",\"options\":[],\"url\":\"5-6109-saagiedatagovernance.prod.saagie.io\",\"cpu\":0.6,\"memory\":1024,\"disk\":1024,\"port\":8080,\"enableAuth\":true,\"authUsername\":\"saagie\"}],\"streaming\":true,\"category\":\"dataviz\",\"name\":\"Saagie Datagovernance\",\"email\":\"\",\"platform_id\":5,\"manual\":true,\"retry\":\"\",\"last_state\":{\"id\":479474,\"state\":\"RUNNING\",\"date\":\"2017-09-27T14:29:36+00:00\",\"lastTaskStatus\":\"PENDING\",\"lastTaskId\":951901},\"workflows\":[],\"deletable\":true},{\"id\":6115,\"capsule_code\":\"python\",\"current\":{\"id\":14691,\"job_id\":6115,\"number\":1,\"template\":\"python {file} impala dn1\",\"file\":\"python-tests-1.0.0.zip\",\"creation_date\":\"2017-09-28T07:58:35+00:00\",\"options\":{\"language_version\":\"2.7.13\"},\"cpu\":0.3,\"memory\":512,\"disk\":512,\"releaseNote\":\"\"},\"versions\":[{\"id\":14691,\"job_id\":6115,\"number\":1,\"template\":\"python {file} impala dn1\",\"file\":\"python-tests-1.0.0.zip\",\"creation_date\":\"2017-09-28T07:58:35+00:00\",\"options\":{\"language_version\":\"2.7.13\"},\"cpu\":0.3,\"memory\":512,\"disk\":512,\"releaseNote\":\"\"}],\"streaming\":false,\"category\":\"processing\",\"name\":\"test connexion impala ldap\",\"email\":\"\",\"always_email\":false,\"platform_id\":5,\"manual\":true,\"schedule\":\"R0\\/2017-09-28T07:58:10.677Z\\/P0Y0M1DT0H0M0S\",\"retry\":\"\",\"last_state\":{\"id\":479853,\"state\":\"STOPPED\",\"date\":\"2017-09-28T08:00:35+00:00\",\"lastTaskStatus\":\"SUCCESS\",\"lastTaskId\":951952},\"workflows\":[],\"deletable\":true},{\"id\":6116,\"capsule_code\":\"sqoop\",\"current\":{\"id\":14692,\"job_id\":6116,\"number\":1,\"template\":\"echo test\",\"creation_date\":\"2017-09-28T07:59:50+00:00\",\"options\":[],\"cpu\":0.3,\"memory\":512,\"disk\":512,\"releaseNote\":\"\"},\"versions\":[{\"id\":14692,\"job_id\":6116,\"number\":1,\"template\":\"echo test\",\"creation_date\":\"2017-09-28T07:59:50+00:00\",\"options\":[],\"cpu\":0.3,\"memory\":512,\"disk\":512,\"releaseNote\":\"\"}],\"streaming\":false,\"category\":\"extract\",\"name\":\"test saagie\",\"email\":\"\",\"always_email\":false,\"platform_id\":5,\"manual\":true,\"schedule\":\"R0\\/2017-09-28T07:59:43.512Z\\/P0Y0M1DT0H0M0S\",\"retry\":\"\",\"last_state\":{\"id\":479851,\"state\":\"STOPPED\",\"date\":\"2017-09-28T07:59:56+00:00\",\"lastTaskStatus\":\"SUCCESS\",\"lastTaskId\":951954},\"workflows\":[],\"deletable\":true},{\"id\":6118,\"capsule_code\":\"talend\",\"current\":{\"id\":14694,\"job_id\":6118,\"number\":1,\"template\":\"sh {file} arg1 arg2\",\"file\":\"Test_talend_1.2.zip\",\"creation_date\":\"2017-09-28T08:01:30+00:00\",\"options\":[],\"cpu\":0.3,\"memory\":512,\"disk\":512,\"releaseNote\":\"\"},\"versions\":[{\"id\":14694,\"job_id\":6118,\"number\":1,\"template\":\"sh {file} arg1 arg2\",\"file\":\"Test_talend_1.2.zip\",\"creation_date\":\"2017-09-28T08:01:30+00:00\",\"options\":[],\"cpu\":0.3,\"memory\":512,\"disk\":512,\"releaseNote\":\"\"}],\"streaming\":false,\"category\":\"extract\",\"name\":\"test saagie\",\"email\":\"\",\"always_email\":false,\"platform_id\":5,\"manual\":true,\"schedule\":\"R0\\/2017-09-28T08:01:18.763Z\\/P0Y0M1DT0H0M0S\",\"retry\":\"\",\"last_state\":{\"id\":479859,\"state\":\"STOPPED\",\"date\":\"2017-09-28T08:01:43+00:00\",\"lastTaskStatus\":\"SUCCESS\",\"lastTaskId\":951956},\"workflows\":[],\"deletable\":true}]"))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobs = saagieClient.getAllJobs()

        then:
        noExceptionThrown()
        jobs == [6109, 6115, 6116, 6118]
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Get manager status'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def code = saagieClient.getManagerStatus()

        then:
        noExceptionThrown()
        code == 200
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Create HttpClient with proxy host but no proxy port'() {
        given:
        def pluginProperties = Spy(SaagiePluginProperties)
        pluginProperties.server.proxyHost = 'localhost'

        when:
        def saagieClient = new SaagieClient(pluginProperties)

        then:
        noExceptionThrown()
        saagieClient.okHttpClient.proxy() == null
    }

    def 'Create HttpClient without proxy host but with proxy port'() {
        given:
        def pluginProperties = Spy(SaagiePluginProperties)
        pluginProperties.server.proxyPort = 4567

        when:
        def saagieClient = new SaagieClient(pluginProperties)

        then:
        noExceptionThrown()
        saagieClient.okHttpClient.proxy() == null
    }

    def 'Create HttpClient with proxy'() {
        given:
        def pluginProperties = Spy(SaagiePluginProperties)
        pluginProperties.server.proxyHost = 'localhost'
        pluginProperties.server.proxyPort = 4567

        when:
        def saagieClient = new SaagieClient(pluginProperties)

        then:
        noExceptionThrown()
        saagieClient.okHttpClient.proxy().address().toString() == 'localhost:4567'
    }

    def 'Create HTTPClient with selfSigned certs acceptation'() {
        given:
        def pluginProperties = Spy(SaagiePluginProperties)
        pluginProperties.server.acceptSelfSigned = true

        when:
        def saagieClient = new SaagieClient(pluginProperties)

        then:
        noExceptionThrown()
        saagieClient.okHttpClient.certificateChainCleaner.trustRootIndex.subjectToCaCerts == [:]
    }

    def 'Upload file with error'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.uploadFile(Paths.get(getClass().getClassLoader().getResource('upload.txt').path))

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job creation at file upload (ErrorCode: 400)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/upload'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Upload file success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setBody('{"fileName": "59d2142c17491/python-tests-1.0.0.zip"}')
                .setResponseCode(200))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def fileName = saagieClient.uploadFile(Paths.get(getClass().getClassLoader().getResource('upload.txt').path))

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/upload'
        request.getHeader('Content-Type').matches('multipart/form-data; boundary=.*')
        fileName == '59d2142c17491/python-tests-1.0.0.zip'

        cleanup:
        mockWebServer.shutdown()
    }


    def "Retrieve job's informations"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}'))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobInfo = saagieClient.getJob(6199)

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.method == 'GET'
        request.path == '/platform/1/job/6199'
        jobInfo == '{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}'

    }

    def "Retrieve job's informations with error"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.getJob(6199)

        then:
        def exception = thrown(GradleException)
        exception.message == 'Impossible to find job 6199 (ErrorCode: 400)'
        def request = mockWebServer.takeRequest()
        request.method == 'GET'
        request.path == '/platform/1/job/6199'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Delete job with error'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.deleteJob(6199)

        then:
        def exception = thrown(GradleException)
        exception.message == 'Error during job deletion(ErrorCode: 400)'
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/6199'
        request.method == 'DELETE'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Delete job with success'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.deleteJob(6199)

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/6199'
        request.method == 'DELETE'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Update job version'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.currentVersion(6135, 5)

        then:
        noExceptionThrown()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/6135/version/5/rollback'
    }

    def 'Retrieve artifact no directory access'() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}'))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.retrieveJobsArtifacts(6199, '')

        then:
        def exception = thrown(GradleException)
        exception.message == 'Impossible to create work directory: /exports/6199-Impala_Connection'

        cleanup:
        mockWebServer.shutdown()
    }

    def 'Retrieve artifact success'() {
        given:
        def stream = getClass().getClassLoader().getResource('upload.txt').openStream()
        def buffer = new Buffer()
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}'))
        mockWebServer.enqueue(new MockResponse()
                .setBody(buffer.readFrom(stream)))
        mockWebServer.start()
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        saagieClient.retrieveJobsArtifacts(6199, './createArchive/')

        then:
        noExceptionThrown()
        mockWebServer.takeRequest()
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/6199/version/1/binary'
        def file = new File('./createArchive/exports').listFiles().head()
        file.name == '6199-Impala_Connection'
        file.list().contains('settings.json')
        file.list().contains('1-python-tests-1.0.0.txt')
        JsonOutput.toJson(new JsonSlurper().parseText(file.listFiles().find {
            it.name == 'settings.json'
        }.text)) == '{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}'
        file.listFiles().find {
            it.name == '1-python-tests-1.0.0.txt'
        }.text == "Test content\n"


        cleanup:
        mockWebServer.shutdown()
        new File('./createArchive').deleteDir()
    }

    def "Export an archive"() {
        given:
        def stream = getClass().getClassLoader().getResource('upload.txt').openStream()
        def buffer = new Buffer()
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}'))
        mockWebServer.enqueue(new MockResponse()
                .setBody(buffer.readFrom(stream)))
        mockWebServer.start()
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'
        saagieClient.configuration.packaging.exportFile = 'test'
        saagieClient.configuration.target = './createZip/'

        when:
        saagieClient.exportArchive(6199, './createZip/')

        then:
        noExceptionThrown()
        1 * saagieClient.retrieveJobsArtifacts(6199, './createZip/')
        def file = new File('./createZip').listFiles().find {
            it.name == '6199-test.zip'
        }
        file.exists()

        cleanup:
        mockWebServer.shutdown()
        new File('./createZip').deleteDir()
    }

    def "Export fat archive"() {
        given:
        def stream = getClass().getClassLoader().getResource('upload.txt').openStream()
        def buffer = new Buffer()
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('[{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.zip","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}]')
        )
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody('{"id":6199,"capsule_code":"python","current":{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""},"versions":[{"id":14983,"job_id":6199,"number":1,"template":"python {file} impala dn1 dn2","file":"python-tests-1.0.0.txt","creation_date":"2017-10-02T12:15:01+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":""}],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":6,"manual":true,"schedule":"R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}'))
        mockWebServer.enqueue(new MockResponse()
                .setBody(buffer.readFrom(stream)))
        mockWebServer.start()
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'
        saagieClient.configuration.packaging.exportFile = 'test'
        saagieClient.configuration.target = './createFatZip/'

        when:
        saagieClient.exportAllArchives('./createFatZip/')

        then:
        noExceptionThrown()
        1 * saagieClient.retrieveJobsArtifacts(6199, './createFatZip/')
        def file = new File('./createFatZip').listFiles().find {
            it.name == 'test-fat.zip'
        }
        file.exists()

        cleanup:
        mockWebServer.shutdown()
        new File('./createFatZip').deleteDir()
    }

    //TODO: Split in two tests but hey...
    def "Archive process unsupported type"() {
        given:
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        def zipDocker = new ZipFile(new File(getClass().classLoader.getResource("docker.zip").file))
        def zipJupyter = new ZipFile(new File(getClass().classLoader.getResource("jupyter.zip").file))

        when:
        saagieClient.processArchive('./processDocker/', zipDocker)
        saagieClient.processArchive('./processJupyter/', zipJupyter)

        then:
        !new File('./processDocker/').exists()
        !new File('./processJupyter/').exists()

        cleanup:
        zipDocker.close()
        zipJupyter.close()
    }

    def "Archive process single version"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setBody('{"fileName": "59d2142c17491/python-tests-1.0.0.txt"}')
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":135,"capsule_code":"python","current":{"id":234,"number":1,"template":"python {file} impala dn1","file":"python-tests-1.0.0.zip","creation_date":"2017-09-25T16:04:47+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","important":false},"versions":[],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":2,"manual":true,"schedule":"R0\\/2017-05-23T13:59:05.587Z\\/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}')
        )
        mockWebServer.start()
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'
        saagieClient.configuration.packaging.importFile = getClass().classLoader.getResource("test.zip").file

        when:
        saagieClient.importArchive('./singleVersion/')

        then:
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/upload'
        request.getHeader('Content-Type').matches('multipart/form-data; boundary=.*')
        1 * saagieClient.createJob(_ as String)

        cleanup:
        new File('./singleVersion/').deleteDir()
        mockWebServer.shutdown()
    }

    def "Archive process fat archive"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse()
                .setBody('{"fileName": "59d2142c17491/python-tests-1.0.0.txt"}')
                .setResponseCode(200))
        mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody('{"id":135,"capsule_code":"python","current":{"id":234,"number":1,"template":"python {file} impala dn1","file":"python-tests-1.0.0.zip","creation_date":"2017-09-25T16:04:47+00:00","options":{"language_version":"2.7.13"},"cpu":0.3,"memory":512,"disk":512,"releaseNote":"","important":false},"versions":[],"streaming":false,"category":"processing","name":"Impala Connection","email":"","platform_id":2,"manual":true,"schedule":"R0\\/2017-05-23T13:59:05.587Z\\/P0Y0M1DT0H0M0S","retry":"","workflows":[],"deletable":true,"description":""}')
        )
        mockWebServer.start()
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'
        saagieClient.configuration.packaging.importFile = getClass().classLoader.getResource("test-fat.zip").file

        when:
        saagieClient.importFatArchive('./fatArchive/')

        then:
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/job/upload'
        request.getHeader('Content-Type').matches('multipart/form-data; boundary=.*')
        1 * saagieClient.createJob(_ as String)

        cleanup:
        new File('./fatArchive/').deleteDir()
        mockWebServer.shutdown()
    }

    def "List vars with empty response"() {
        given:
        def mockWebServer = new MockWebServer()
        mockWebServer.enqueue(new MockResponse())
        mockWebServer.start()
        def saagieClient = Spy(SaagieClient, constructorArgs: [Spy(SaagiePluginProperties)])
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobs = saagieClient.getAllVars()

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
        def saagieClient = new SaagieClient(Spy(SaagiePluginProperties))
        saagieClient.configuration.server.url = "http://$mockWebServer.hostName:$mockWebServer.port"
        saagieClient.configuration.server.platform = '1'

        when:
        def jobs = saagieClient.getAllVars()

        then:
        noExceptionThrown()
        jobs == [["id": 208, "name": "MONGO_PORT", "value": "27017", "isPassword": false, "platformId": 12], ["id": 782, "name": "MYSQL_USER_PASSWORD", "isPassword": true, "platformId": 12]]
        def request = mockWebServer.takeRequest()
        request.path == '/platform/1/envvars'

        cleanup:
        mockWebServer.shutdown()
    }
}
