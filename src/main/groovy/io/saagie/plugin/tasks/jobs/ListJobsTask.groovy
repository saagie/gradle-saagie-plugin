package io.saagie.plugin.tasks.jobs

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.SaagieClient
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

/**
 * List all jobs on platform.
 * Created by ekoffi on 7/3/17.
 */
class ListJobsTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def listJobs() {
        new ListJobs(configuration).listJobs(logger)
    }
}

class ListJobs {
    SaagiePluginProperties configuration
    SaagieClient saagieClient

    ListJobs(SaagiePluginProperties configuration) {
        this.configuration = configuration
        this.saagieClient = new SaagieClient(configuration)
    }

    def listJobs(Logger logger) {
        logger.info("List jobs.")
        logger.quiet("Job list: {}", saagieClient.allJobs)
    }
}
