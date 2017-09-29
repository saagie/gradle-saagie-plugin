package io.saagie.plugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.*
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.ssl.SSLContexts
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

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
    /**
     * Logger
     */
    private static final Logger logger = Logging.getLogger(this.class)

    /**
     * Media type for JSON constant.
     */
    private static final JSON_MEDIA_TYPE = MediaType.parse("application/json")

    /**
     * Media type for form data type, used for file upload.
     */
    private static final FORM_DATA_MEDIA_TYPE = MediaType.parse("multipart/form-data")

    /**
     * The configuration used by the client.
     */
    SaagiePluginProperties configuration

    /**
     * The Http REST client.
     */
    OkHttpClient okHttpClient

    /**
     * Used to parse JSON.
     */
    JsonSlurper jsonSlurper

    /**
     * A REST client wrapper for DataFabric's API calls.
     * Self signed certs acceptation and proxies are set here and can't be modified after client creation.
     * @param configuration The configuration to use to connect to Saagie's platform and manage jobs.
     */
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

    /**
     * Calls the manager to check it's status.
     * @return The HTTP response code from DataFabric.
     */
    int getManagerStatus() {
        def request = new Request.Builder()
                .url("${configuration.server.url}/platform/${configuration.server.platform}")
                .get()
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        def responseCode = response.code()
        logger.info("Platform status: {}", responseCode)
        response.close()
        return responseCode
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
     * Calls DataFabric's api to create a job
     * @param body the request body, should comply with current api version.
     * @return The id of the newly created job
     */
    int createJob(String body) {
        logger.info("Create Job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job")
                .post(RequestBody.create(JSON_MEDIA_TYPE, body))
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        if (response.isSuccessful()) {
            def responseText = response.body().string()
            def jsonResponse = jsonSlurper.parseText responseText
            logger.info("Response: {}", responseText)
            return jsonResponse.id
        } else {
            throw new GradleException("Error during job creation(ErrorCode: ${response.code()})")
        }
    }

    /**
     * Update job on platform.
     * @param id The id of the job to update
     * @param job the job configuration to update.
     */
    void updateJob(int id, String job) {
        logger.info("Update Job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$id/version")
                .post(RequestBody.create(JSON_MEDIA_TYPE, job))
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        logger.info("{}", JsonOutput.prettyPrint(job).stripIndent())

        if (response.isSuccessful()) {
            logger.info("Job updated. {}", response.body().string())
        } else {
            throw new GradleException("Error during job update(ErrorCode: ${response.code()})")
        }
    }

    /**
     * Delete a job.
     */
    void deleteJob() {
        logger.info("Delete Job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$configuration.job.id")
                .delete()
                .build()
        def response = okHttpClient.newCall(request).execute()
        if (response.code() != 200 && response.code() != 204) {
            throw new GradleException("Error during job deletion(ErrorCode: {${response.code()})")
        } else {
            logger.info("Job deleted.")
        }
    }

    /**
     * Retrieves a job settings.
     * @return JSON String representation of job.
     */
    String getJob(int id) {
        logger.info("Check job {} exists", configuration.job)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$id")
                .get()
                .build()

        def response = okHttpClient.newCall(request).execute()
        return response.body().string()
    }

    /**
     * Returns a platform's complete job id list.
     * @return
     */
    List<Integer> getAllJobs() {
        logger.info("Returns job list for platform {}", configuration.server.platform)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job")
                .get()
                .build()

        def response = okHttpClient.newCall(request).execute()
        def content = response.body().string()
        logger.info("Response: {}", content)
        def result = []
        if (!content.empty) {
            result = jsonSlurper
                    .parseText(content)
                    .collect { it.id }
        }

        return result
    }

    /**
     * Creates an archive for a job.
     * @param buildDir directory where the plugin will work.
     * @return The path to the directory which contains all packages and job's settings.
     */
    File archiveCreation(String buildDir) {
        logger.info("Creates an archive job.")
        String job = getJob(configuration.job.id)
        if (job.length() == 0) {
            throw new GradleException("Job does not exists: $configuration.job.id")
        }
        logger.info("{}", job)
        try {

            def jsonJob = jsonSlurper.parseText job
            def name = "$jsonJob.id-${jsonJob.name.replaceAll(' ', '_').replaceAll('/', '#')}"
            def workDir = new File("$buildDir/exports/$name")
            workDir.delete()
            workDir.mkdirs()
            if (!workDir.exists()) {
                throw new GradleException("Impossible to create work directory.")
            }
            new File(workDir, "settings.json").write(JsonOutput.prettyPrint(job))
            if (jsonJob.capsule_code != 'sqoop' && jsonJob.capsule_code != 'docker' && jsonJob.capsule_code != 'jupiter') {
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
                    new File(workDir, "$it.number-$it.file").bytes = response.body().byteStream().bytes
                }
            }
            return workDir
        } catch (Exception ex) {
            throw new GradleException("Impossible to create archive: {}", ex)
        }
    }

    /**
     * Create an archive with job description and files.
     */
    void exportArchive(String buildDir) {
        logger.info("Archives a job.")
        File workDir = archiveCreation(buildDir)
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File(configuration.target, "${configuration.packaging.exportFile}.zip")))
        workDir.eachFile {
            zip.putNextEntry(new ZipEntry(it.getName()))
            zip.write(it.readBytes())
            zip.closeEntry()
        }
        zip.close()
        workDir.deleteDir()
    }

    /**
     * Exports all archives from a platform.
     * @param buildDir the directory where the plugin will work.
     */
    void exportAllArchives(String buildDir) {
        logger.info("Archive all jobs.")
        def jobs = getAllJobs()
        if (jobs.empty) {
            throw new GradleException("Platform is empty.")
        }
        configuration.packaging.currentOnly = false
        logger.lifecycle("Number of jobs to export: {}", jobs.size())
        int cpt = 0
        jobs.each {
            configuration.job.id = it
            File workDir = archiveCreation(buildDir)
            ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File("$buildDir/exports", "${workDir.canonicalFile.name}.zip")))
            workDir.eachFile {
                zip.putNextEntry(new ZipEntry(it.getName()))
                zip.write(it.readBytes())
                zip.closeEntry()
            }
            zip.close()
            workDir.deleteDir()
            cpt++
            logger.lifecycle("Processed: {} / {}", cpt, jobs.size())
        }
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(new File(configuration.target, "${configuration.packaging.exportFile}-fat.zip")))
        new File(buildDir, "exports").eachFile { file ->
            if (!file.isDirectory()) {
                zip.putNextEntry(new ZipEntry(file.getName()))
                zip.write(file.readBytes())
                zip.closeEntry()
            }
        }
        zip.close()
    }

    void archiveProcess(String buildDir, ZipFile zip) {
        def settings = jsonSlurper.parseText(zip.getInputStream(zip.getEntry("settings.json")).text)
        if (settings.capsule_code != 'docker' && settings.capsule_code != 'jupiter') {
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
            def id = 0
            versions.findAll {
                (!configuration.packaging.currentOnly || it.number == current.number)
            } each { version ->
                try {
                    settings.current = version
                    if (settings.capsule_code != 'sqoop') {
                        logger.info("File searched: {}", "$version.number-$version.file")
                        def file = zip.getInputStream(zip.getEntry("$version.number-$version.file"))
                        def path = Files.write(Paths.get(buildDir, "$version.file"), file.bytes)
                        file.close()
                        def fileName = uploadFile(path)
                        version.file = fileName
                        path.deleteDir()
                    }
                    version.remove("id")
                    version.remove("creation_date")
                    version.remove("number")
                    def jsonSettings = JsonOutput.toJson(settings)
                    logger.info(JsonOutput.prettyPrint(jsonSettings).stripIndent())
                    if (first) {
                        id = createJob(jsonSettings)
                        first = false
                    } else {
                        updateJob(id, jsonSettings)
                    }
                    zip.close()
                } catch (Exception ex) {
                    logger.error("Version: {}", version)
                    logger.error("Impossible to save version: ", ex)
                }
            }
        }
    }

    /**
     * Creates jobs on platform from archive.
     */
    void importArchive(String buildDir) {
        logger.info("Import archive.")
        archiveProcess(buildDir, new ZipFile(new File(configuration.target, configuration.packaging.importFile)))
    }

    /**
     * Imports a fat archive.
     * @param buildDir
     */
    void importFatArchive(String buildDir) {
        logger.info("Import fat archive.")
        ZipFile zip = new ZipFile(new File(configuration.target, configuration.packaging.importFile))
        def workDir = new File("$buildDir/imports")
        workDir.deleteDir()
        workDir.mkdirs()
        zip.entries().each {
            def zipStream = zip.getInputStream(it)
            def file = new File("$workDir/$it.name")
            file.bytes = zipStream.bytes
            archiveProcess(buildDir, new ZipFile(file))
            zipStream.close()
        }
        zip.close()
    }

}
