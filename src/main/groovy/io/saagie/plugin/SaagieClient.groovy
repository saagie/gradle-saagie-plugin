package io.saagie.plugin

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import org.apache.http.HttpHost
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.impl.nio.client.HttpAsyncClients
import org.apache.http.ssl.SSLContexts
import org.gradle.api.GradleException
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.net.ssl.SSLContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.Future
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

    SaagiePluginProperties configuration

    SaagieClient(SaagiePluginProperties configuration) {
        this.configuration = configuration
        boolean proxy = !configuration.server.proxyHost.isEmpty()
        HttpHost httpHost = null
        if (proxy) {
            httpHost = new HttpHost(configuration.server.proxyHost, configuration.server.proxyPort)
        }
        if (configuration.server.acceptSelfSigned) {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy() {
                @Override
                boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    true
                }
            }).build()

            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())

            HttpAsyncClientBuilder httpAsyncClientBuilder = HttpAsyncClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())

            if (proxy) {
                httpClientBuilder.setProxy(httpHost)
                httpAsyncClientBuilder.setProxy(httpHost)
            }

            Unirest.setHttpClient(httpClientBuilder.build())
            Unirest.setAsyncHttpClient(httpAsyncClientBuilder.build())
        } else if (proxy) {
            Unirest.setProxy(httpHost)
        }
    }

    void getManagerStatus() {
        Future<HttpResponse<JsonNode>> future = Unirest.get("${configuration.server.url}/platform/${configuration.server.platform}")
                .basicAuth(configuration.server.login, configuration.server.password)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        logger.info("Platform status: $response.status")
    }

    /**
     * Upload given file to platform.
     * @param path
     * @return The file name on the platform.
     */
    String uploadFile(Path path) {
        logger.info("Upload file.")
        File file = path.toFile()
        logger.info("File: {}", file.toString())
        Future<HttpResponse<JsonNode>> future = Unirest.post("$configuration.server.url/platform/$configuration.server.platform/job/upload")
                .basicAuth(configuration.server.login, configuration.server.password)
                .field("file", file, "multipart/form-data")
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.MINUTES)
        if (response.status != 200) {
            throw new GradleException("Error during job creation(ErrorCode: $response.status)")
        } else {
            return response.body.object.getString("fileName")
        }
    }

    /**
     * Create job on platform.
     * @param body the request body, should comply with current api version.
     * @return The id of the newly created job
     */
    int createJob(JSONObject body) {
        logger.info("Create Job.")
        Future<HttpResponse<JsonNode>> future = Unirest.post("$configuration.server.url/platform/$configuration.server.platform/job")
                .basicAuth(configuration.server.login, configuration.server.password)
                .body(body)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        if (response.status != 200) {
            throw new GradleException("Error during job creation(ErrorCode: $response.status)")
        } else {
            return response.body.object.getInt("id")
        }
    }

    void updateJob(JSONObject job) {
        logger.info("Update Job.")
        Future<HttpResponse<JsonNode>> future = Unirest.post("$configuration.server.url/platform/$configuration.server.platform/job/$configuration.job.id/version")
                .basicAuth(configuration.server.login, configuration.server.password)
                .body(job)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        if (response.status != 200 && response.status != 201) {
            throw new GradleException("Error during job creation(ErrorCode: $response.status)")
        } else {
            logger.info("Job updated. ${response.body.object.toString(4)}")
        }

    }

    JSONObject checkJobExists() {
        logger.debug("Check job {} exists", configuration.job)
        Future<HttpResponse<JsonNode>> future = Unirest.get("$configuration.server.url/platform/$configuration.server.platform/job/$configuration.job.id")
                .basicAuth(configuration.server.login, configuration.server.password)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        return response.body.object
    }

    void createArchive() {
        logger.info("Archives a job.")
        JSONObject job = checkJobExists()
        if (job.length() == 0) {
            throw new GradleException("Job does not exists.")
        }
        logger.info(job.toString(4))
        File path = new File(configuration.target, configuration.packaging.exportFile)
        path.mkdir()
        new File(path.toString(), "settings.json").write(job.toString(4))
        if (job.getString("capsule_code") != 'sqoop') {
            job.getJSONArray("versions").findAll {
                (!configuration.packaging.currentOnly || ((JSONObject) it).getInt("number") == job.getJSONObject("current").getInt("number"))
            } each {
                int version = ((JSONObject) it).getInt('number')
                String fileName = ((JSONObject) it).getString('file')
                Future<HttpResponse<InputStream>> future = Unirest.get("$configuration.server.url/platform/$configuration.server.platform/job/$configuration.job.id/version/$version/binary")
                        .basicAuth(configuration.server.login, configuration.server.password)
                        .asBinaryAsync()
                HttpResponse<InputStream> response = future.get(10, TimeUnit.MINUTES)
                new File(path.toString(), "$version-$fileName").bytes = response.rawBody.bytes
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

    void importArchive() {
        logger.info("Import archive.")
        ZipFile zip = new ZipFile(new File(configuration.target, configuration.packaging.importFile))
        JSONObject settings = new JSONObject(zip.getInputStream(zip.getEntry("settings.json")).text)
        settings.remove("id")
        settings.remove("platform_id")
        settings.remove("last_state")
        settings.remove("last_instance")
        settings.remove("workflows")
        JSONArray versions = settings.getJSONArray("versions")
        JSONObject current = settings.getJSONObject("current")
        settings.remove("versions")
        settings.remove("current")
        def first = true
        versions.findAll {
            (!configuration.packaging.currentOnly || ((JSONObject) it).getInt("number") == current.getInt("number"))
        } each { JSONObject version ->
            def prefix = version.getInt("number")
            def fileName = version.getString("file")
            def file = zip.getInputStream(zip.getEntry("$prefix-${version.getString("file")}"))
            version.remove("id")
            version.remove("number")
            version.remove("creation_date")
            settings.put("current", version)
            if (settings.getString('capsule_code') != 'sqoop') {
                def path = Files.write(Paths.get("$fileName"), file.bytes)
                file.close()
                fileName = uploadFile(path)
                version.put("file", fileName)
                path.deleteDir()
            }
            logger.info(settings.toString(4))
            if (first) {
                configuration.job.id = createJob(settings)
                first = false
            } else {
                updateJob(settings)
            }
        }
        zip.close()
    }
}
