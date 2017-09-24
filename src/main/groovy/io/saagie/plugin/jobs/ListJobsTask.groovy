package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * List all jobs on platform.
 * Created by ekoffi on 7/3/17.
 */
class ListJobsTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def listJobs() {
        logger.info("List jobs.")
        SaagieClient saagieClient = new SaagieClient(configuration)
        logger.quiet("Job list: {}", saagieClient.allJobs)
    }
}
