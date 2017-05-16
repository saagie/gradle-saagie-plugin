package io.saagie.plugin

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import org.gradle.api.GradleException
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Created by ekoffi on 5/15/17.
 */
class SaagieClient {
    private static final Logger logger = LoggerFactory.getLogger(this.class)

    SaagiePluginProperties configuration

    SaagieClient(SaagiePluginProperties configuration) {
        this.configuration = configuration
    }

    void getManagerStatus() {
        Future<HttpResponse<JsonNode>> future = Unirest.get("$configuration.url/platform/$configuration.platform")
                .basicAuth(configuration.login, configuration.password)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        logger.info("Platform status: $response.status")
    }

    int createJob(JSONObject body) {
        logger.info("Create Job.")
        Future<HttpResponse<JsonNode>> future = Unirest.post("$configuration.url/platform/$configuration.platform/job")
                .basicAuth(configuration.login, configuration.password)
                .body(body)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        if (response.status != 200) {
            throw new GradleException("Error during job creation(ErrorCode: $response.status)")
        } else {
            return response.body.object.getInt("id")
        }
    }

    String uploadFile(String path, String fileName) {
        logger.info("Upload file.")
        File file = Paths.get(path, fileName).toFile()
        logger.info("File: {}", file.toString())
        Future<HttpResponse<JsonNode>> future = Unirest.post("$configuration.url/platform/$configuration.platform/job/upload")
                .basicAuth(configuration.login, configuration.password)
                .field("file", file, "multipart/form-data")
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(60, TimeUnit.SECONDS)
        if (response.status != 200) {
            throw new GradleException("Error during job creation(ErrorCode: $response.status)")
        } else {
            return response.body.object.getString("fileName")
        }
    }

    JSONObject checkJobExists() {
        logger.debug("Check job {} exists", configuration.job)
        Future<HttpResponse<JsonNode>> future = Unirest.get("$configuration.url/platform/$configuration.platform/job/$configuration.job")
                .basicAuth(configuration.login, configuration.password)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        return response.body.object
    }

    void updateJob(JSONObject job) {
        logger.info("Update Job.")
        Future<HttpResponse<JsonNode>> future = Unirest.post("$configuration.url/platform/$configuration.platform/job/$configuration.job/version")
                .basicAuth(configuration.login, configuration.password)
                .body(job)
                .asJsonAsync()
        HttpResponse<JsonNode> response = future.get(10, TimeUnit.SECONDS)
        if (response.status != 200 && response.status != 201) {
            throw new GradleException("Error during job creation(ErrorCode: $response.status)")
        } else {
            logger.info("Job updated. {}", response.body.object.toString(4))
        }

    }
}
