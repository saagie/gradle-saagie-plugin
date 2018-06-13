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
class RunJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def runJob() {
        new RunJob(configuration).runJob(logger)
    }
}

class RunJob {
    SaagiePluginProperties configuration
    ExecutionClient executionClient

    RunJob(SaagiePluginProperties configuration) {
        this.configuration = configuration
        executionClient = new ExecutionClient(configuration)
    }

    def runJob(Logger logger) {
        logger.info("Running job.")
        executionClient.getManagerStatus()
        configuration.jobs.each { job ->
            def id = job.findId()
            executionClient.jobManagement(id, RunningAction.run)
        }
    }
}