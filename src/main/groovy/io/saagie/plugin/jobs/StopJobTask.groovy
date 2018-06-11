package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
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
    SaagieClient saagieClient

    StopJob(SaagiePluginProperties configuration) {
        this.configuration = configuration
        saagieClient = new SaagieClient(configuration)
    }

    def stopJob(Logger logger) {
        logger.info("Running job.")
        saagieClient.getManagerStatus()
        configuration.jobs.each { job ->
            def id = job.findId()
            saagieClient.runJob(id)
        }
    }
}