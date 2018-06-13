package io.saagie.plugin.tasks.execution

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.ExecutionClient
import io.saagie.plugin.properties.RunningAction
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

/**
 * Created by ekoffi on 6/11/18.
 */
class StopJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def stopJob() {
        new StopJob(configuration).stopJob(logger)
    }
}

class StopJob {
    SaagiePluginProperties configuration
    ExecutionClient executionClient

    StopJob(SaagiePluginProperties configuration) {
        this.configuration = configuration
        executionClient = new ExecutionClient(configuration)
    }

    def stopJob(Logger logger) {
        logger.info("S Stopping job.")
        executionClient.getManagerStatus()
        configuration.jobs.each { job ->
            def id = job.findId()
            executionClient.jobManagement(id, RunningAction.stop)
        }
    }
}