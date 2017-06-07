package io.saagie.plugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.*
import org.gradle.api.GradleException
import org.gradle.internal.impldep.org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.gradle.internal.impldep.org.apache.http.ssl.SSLContexts
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Saagie manager REST API Client
 * Created by ekoffi on 5/15/17.
 */
class SaagieClient {
    private static final Logger logger = LoggerFactory.getLogger(this.class)

    private static final JSON_MEDIA_TYPE = MediaType.parse("application/json")
    private static final FORM_DATA_MEDIA_TYPE = MediaType.parse("multipart/form-data")

    SaagiePluginProperties configuration
    OkHttpClient okHttpClient
    JsonSlurper jsonSlurper

    SaagieClient(SaagiePluginProperties configuration) {
        this.configuration = configuration
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
        if (!configuration.server.proxyHost.isEmpty()) {
            builder.proxy(new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved(configuration.server.proxyHost, configuration.server.proxyPort)))
        }
        if (configuration.server.acceptSelfSigned) {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
                @Override
                boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    true
                }
            }).build()
            builder.sslSocketFactory(sslContext.getSocketFactory(),
                    new X509TrustManager() {
                        @Override
                        void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0]
                        }
                    })
        }

        builder.authenticator(new Authenticator() {
            @Override
            Request authenticate(Route route, Response response) throws IOException {
                return response.request().newBuilder().header('Authorization', Credentials.basic(configuration.server.login, configuration.server.password)).build()
            }
        })
        builder.connectTimeout(2, TimeUnit.SECONDS)
        builder.readTimeout(8, TimeUnit.SECONDS)
        builder.writeTimeout(8, TimeUnit.SECONDS)

        okHttpClient = builder.build()
        jsonSlurper = new JsonSlurper()
    }

    void getManagerStatus() {
        def request = new Request.Builder()
                .url("${configuration.server.url}/platform/${configuration.server.platform}")
                .get()
                .build()
        def response = okHttpClient.newCall(request).execute()
        logger.info("Platform status: ${response.code()}")
    }

    /**
     * Upload given file to platform.
     * @param path
     * @return The file name on the platform.
     */
    String uploadFile(Path path) {
        def client = okHttpClient
                .newBuilder()
                .writeTimeout(10, TimeUnit.MINUTES)
                .build()

        logger.info("Upload file.")
        def file = path.toFile()
        logger.info("File: {}", file.toString())

        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/upload")
                .post(new MultipartBody.Builder()
                .setType(FORM_DATA_MEDIA_TYPE)
                .addFormDataPart("file", file.getName(), RequestBody.create(FORM_DATA_MEDIA_TYPE, file))
                .build())
                .build()

        def response = client.newCall(request).execute()
        if (response.code() != 200) {
            throw new GradleException("Error during job creation at file upload (ErrorCode: ${response.code()})")
        } else {
            def jsonResponse = jsonSlurper.parseText response.body().string()
            return jsonResponse.fileName
        }
    }

    /**
     * Create job on platform.
     * @param body the request body, should comply with current api version.
     * @return The id of the newly created job
     */
    int createJob(String body) {
        logger.info("Create Job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job")
                .post(RequestBody.create(JSON_MEDIA_TYPE, body))
                .build()

        def response = okHttpClient.newCall(request).execute()

        if (response.code() != 200) {
            throw new GradleException("Error during job creation(ErrorCode: ${response.code()})")
        } else {
            def jsonResponse = jsonSlurper.parseText response.body().string()
            return jsonResponse.id
        }
    }

    /**
     * Update job on platform.
     * @param job the job configuration to update.
     */
    void updateJob(String job) {
        logger.info("Update Job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$configuration.job.id/version")
                .post(RequestBody.create(JSON_MEDIA_TYPE, job.toString()))
                .build()
        def response = okHttpClient.newCall(request).execute()
        if (response.code() != 200 && response.code() != 201) {
            throw new GradleException("Error during job creation(ErrorCode: ${response.code()})")
        } else {
            logger.info("Job updated. ${response.body().string()}")
        }
    }

    String checkJobExists() {
        logger.debug("Check job {} exists", configuration.job)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$configuration.job.id")
                .get()
                .build()

        def response = okHttpClient.newCall(request).execute()
        return response.body().string()
    }

    /**
     * Create an archive with job description and files.
     */
    void createArchive() {
        logger.info("Archives a job.")
        String job = checkJobExists()
        if (job.length() == 0) {
            throw new GradleException("Job does not exists.")
        }
        logger.info(JsonOutput.prettyPrint(job))
        File path = new File(configuration.target, configuration.packaging.exportFile)
        path.mkdir()
        if (!path.exists()) {
            throw new GradleException("Impossible to create path for archive: $path")
        }
        new File(path.toString(), "settings.json").write(JsonOutput.prettyPrint(job))
        def jsonJob = jsonSlurper.parseText job
        if (jsonJob.capsule_code != 'sqoop') {
            def client = okHttpClient
                    .newBuilder()
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build()

            jsonJob.versions.findAll {
                (!configuration.packaging.currentOnly || it.number == jsonJob.current.number)
            } each {
                def request = new Request.Builder()
                        .url("$configuration.server.url/platform/$configuration.server.platform/job/$configuration.job.id/version/${it.number}/binary")
                        .build()
                def response = client.newCall(request).execute()
                new File(path.toString(), "$it.number-$it.file").bytes = response.body().byteStream().bytes
            }
        }
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File(configuration.target, "${configuration.packaging.exportFile}.zip")))
        path.eachFile { file ->
            zip.putNextEntry(new ZipEntry(file.getName()))
            zip.write(file.readBytes())
        }
        zip.closeEntry()
        zip.close()
        path.deleteDir()
    }

    /**
     * Creates jobs on platform from archive.
     */
    void importArchive() {
        logger.info("Import archive.")
        ZipFile zip = new ZipFile(new File(configuration.target, configuration.packaging.importFile))
        def settings = jsonSlurper.parseText(zip.getInputStream(zip.getEntry("settings.json")).text)
        settings.remove("id")
        settings.remove("platform_id")
        settings.remove("last_state")
        settings.remove("last_instance")
        settings.remove("workflows")
        def versions = settings.versions
        def current = settings.current
        settings.remove("versions")
        settings.remove("current")
        def first = true
        versions.findAll {
            (!configuration.packaging.currentOnly || it.number == current.number)
        } each { version ->
            def file = zip.getInputStream(zip.getEntry("$version.number-$version.file"))
            version.remove("id")
            version.remove("number")
            version.remove("creation_date")
            settings.current = version
            if (settings.capsule_code != 'sqoop') {
                def path = Files.write(Paths.get("$version.file"), file.bytes)
                file.close()
                def fileName = uploadFile(path)
                version.file = fileName
                path.deleteDir()
            }
            def jsonSettings = JsonOutput.toJson(settings)
            logger.info(JsonOutput.prettyPrint(jsonSettings).stripIndent())
            if (first) {
                configuration.job.id = createJob(jsonSettings)
                first = false
            } else {
                updateJob(jsonSettings)
            }
        }
        zip.close()
    }
}
