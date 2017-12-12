package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by ekoffi on 5/12/17.
 * Task to delete all jobs on platform.
 */
class DeleteAllJobsTask extends DefaultTask {
    SaagiePluginProperties configuration

    /**
     * Delete all jobs on platform. An usafe flag must be set before the task is available.
     */
    @TaskAction
    def deleteAllJobs() {
        logger.info("Delete Job.")
        SaagieClient saagieClient = new SaagieClient(configuration)
        saagieClient.getManagerStatus()
        if (configuration.unsafe) {
            def jobs = saagieClient.getAllJobs()
            (jobs - configuration.jobs.collect { it.findId() })
            .each {
                saagieClient.deleteJob(it)
            }
            logger.info("All jobs have been deleted.")
        } else {
            logger.error("Unsafe operation, unsafe flag must be checked.")
        }
    }
}
