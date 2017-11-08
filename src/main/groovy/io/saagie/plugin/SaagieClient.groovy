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
        if (!configuration.server.proxyHost.isEmpty() && configuration.server.proxyPort != 0) {
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

        def response = client
                .newCall(request)
                .execute()

        if (response.isSuccessful()) {
            def body = response.body().string()
            logger.info("Upload file response: {}", body)
            def jsonResponse = jsonSlurper.parseText body
            return jsonResponse.fileName
        } else {
            throw new GradleException("Error during job creation at file upload (ErrorCode: ${response.code()})")
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
     * @param id The id of the job to delete
     */
    void deleteJob(int id) {
        logger.info("Delete Job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$id")
                .delete()
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        if (response.isSuccessful()) {
            logger.info("Delete response: {}", response.body().string())
            logger.lifecycle("Job {} deleted.", id)
        } else {
            throw new GradleException("Error during job deletion(ErrorCode: ${response.code()})")
        }
    }

    /**
     * Retrieves a job settings.
     * @param id The id of the job
     * @return JSON String representation of job.
     */
    String getJob(int id) {
        logger.info("Check job {} exists", id)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$id")
                .get()
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        logger.info("Response code: {}", response.code())
        if (response.isSuccessful()) {
            def jsonResponse = response.body().string()
            return jsonResponse
        } else {
            throw new GradleException("Impossible to find job $id (ErrorCode: ${response.code()})")
        }

    }

    /**
     * Updates job's current version.
     * @param id The id of the job to update.
     * @param version version to set.
     * @return The rollback version.
     */
    String currentVersion(int id, int version) {
        logger.info("Set current version")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$id/version/$version/rollback")
                .post(RequestBody.create(JSON_MEDIA_TYPE, '{}'))
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        logger.info("Response code: {}", response.code())

        if (response.isSuccessful()) {
            def jsonResponse = response.body().string()
            return jsonResponse
        } else {
            throw new GradleException("Impossible to find job $id (ErrorCode: ${response.code()})")
        }
    }

    /**
     * Returns a platform's complete job id list.
     * @return Job's id list.
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
     * Returns a platform's complete environment variables list.
     * @return A list of
     */
    List<String> getAllVars() {
        logger.info("Returns var list for platform {}", configuration.server.platform)
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/envvars")
                .get()
                .build()

        def response = okHttpClient.newCall(request).execute()
        def content = response.body().string()
        logger.info("Response: {}", content)
        def result = []
        if (!content.empty) {
            result = jsonSlurper.parseText(content) as List<String>
        }

        return result
    }

    /**
     * Retrieves artifacts for a job.
     * @param id Job id to retrieve
     * @param buildDir directory where the plugin will work.
     * @return The path to the directory which contains all artifacts and job's settings.
     */
    File retrieveJobsArtifacts(int id, String buildDir) {
        logger.info("Creates an archive job.")
        String job = this.getJob(id)
        logger.info("{}", job)

        def jsonJob = jsonSlurper.parseText job
        def name = "$jsonJob.id-${jsonJob.name.replaceAll(' ', '_').replaceAll('/', '#')}"
        def workDir = new File("$buildDir/exports/$name")
        workDir.delete()
        workDir.mkdirs()
        if (!workDir.exists()) {
            throw new GradleException("Impossible to create work directory: $buildDir/exports/$name")
        }
        new File(workDir, "settings.json").write(JsonOutput.prettyPrint(job))
        if (![JobType.SQOOP, JobType.DOCKER, JobType.JUPYTER].contains(jsonJob.capsule_code)) {
            def client = okHttpClient
                    .newBuilder()
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build()

            jsonJob.versions.findAll {
                (!configuration.packaging.currentOnly || it.number == jsonJob.current.number)
            } each {
                def request = new Request.Builder()
                        .url("$configuration.server.url/platform/$configuration.server.platform/job/$id/version/${it.number}/binary")
                        .build()

                def response = client
                        .newCall(request)
                        .execute()

                new File(workDir, "$it.number-$it.file").bytes = response.body().byteStream().bytes
            }
        }
        return workDir
    }

    /**
     * Create an archive with job description and files.
     * @param id Job id to retrieve.
     * @param buildDir directory where the plugin will work.
     */
    void exportArchive(int id, String buildDir) {
        logger.info("Archives a job.")
        def workDir = this.retrieveJobsArtifacts(id, buildDir)
        new File(configuration.target, "$id-${configuration.packaging.exportFile}.zip").withOutputStream {
            ZipOutputStream zip = new ZipOutputStream(it)
            workDir.eachFile {
                zip.putNextEntry(new ZipEntry(it.getName()))
                zip.write(it.readBytes())
                zip.closeEntry()
            }
        }
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
        logger.lifecycle("Number of jobs to export: {}", jobs.size())
        int cpt = 0
        jobs.each {
            File workDir = this.retrieveJobsArtifacts(it, buildDir)
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

    /**
     * Import a job to the platform.
     * @param buildDir The directory where method will work.
     * @param zip The archive of the job to upload.
     */
    void processArchive(String buildDir, ZipFile zip) {
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
                settings.current = version
                if (settings.capsule_code != JobType.SQOOP) {
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
                version.remove("job_id")
                def jsonSettings = JsonOutput.toJson(settings)
                logger.info(JsonOutput.prettyPrint(jsonSettings).stripIndent())
                if (first) {
                    id = createJob(jsonSettings)
                    first = false
                } else {
                    updateJob(id, jsonSettings)
                }
            }

            if (!configuration.packaging.currentOnly) {
                currentVersion(id, (Integer) current.number)
            }
        }
    }

    /**
     * Creates jobs on platform from archive.
     * @param buildDir The directory the method will work in.
     */
    void importArchive(String buildDir) {
        logger.info("Import archive.")
        new File(buildDir).mkdir()
        def zip = new ZipFile(new File(configuration.target, configuration.packaging.importFile))
        this.processArchive(buildDir, new ZipFile(new File(configuration.target, configuration.packaging.importFile)))
        zip.close()
    }

    /**
     * Imports a fat archive.
     * @param buildDir The directory the method will work in.
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
            def zipFile = new ZipFile(file)
            processArchive(buildDir, new ZipFile(file))
            zipFile.close()
            zipStream.close()
        }
        zip.close()
    }

}
