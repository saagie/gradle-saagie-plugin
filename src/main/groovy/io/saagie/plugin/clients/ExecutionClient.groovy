package io.saagie.plugin.clients

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.properties.RunningAction
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.GradleException

/**
 * Saagie manager REST API Client focused on job execution.
 * Created by ekoffi on 6/13/18.
 */
class ExecutionClient extends SaagieClient {

    /**
     * A REST client wrapper for DataFabric's API calls.
     * Self signed certs acceptation and proxies are set here and can't be modified after client creation.
     * @param configuration The configuration to use to connect to Saagie's platform and manage jobs.
     */
    ExecutionClient(SaagiePluginProperties configuration) {
        super(configuration)
    }

    /**
     * Manage job process.
     * @param id The id of the job to manage.
     * @param action The action to apply to the job.
     */
    void jobManagement(int id, RunningAction action) {
        logger.info("$action job.")
        def request = new Request.Builder()
                .url("$configuration.server.url/platform/$configuration.server.platform/job/$id/$action")
                .post(RequestBody.create(JSON_MEDIA_TYPE, '{}'))
                .build()

        def response = okHttpClient
                .newCall(request)
                .execute()

        if (response.isSuccessful()) {
            logger.info("Job $action. {}", response.body().string())
        } else {
            throw new GradleException("Error during job $action (ErrorCode: ${response.code()})")
        }
    }
}