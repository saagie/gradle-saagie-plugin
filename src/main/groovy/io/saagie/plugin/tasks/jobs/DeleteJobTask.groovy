package io.saagie.plugin.tasks.jobs

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.SaagieClient
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

/**
 * Created by ekoffi on 5/12/17.
 * Task to delete a job.
 */
class DeleteJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    /**
     * Delete one job. Job id is set in configuration.
     */
    @TaskAction
    def deleteJob() {
        new DeleteJob(configuration).deleteJob(logger)
    }
}

class DeleteJob {
    SaagiePluginProperties configuration

    SaagieClient saagieClient

    DeleteJob(SaagiePluginProperties configuration) {
        this.configuration = configuration
        saagieClient = new SaagieClient(configuration)
    }

    def deleteJob(Logger logger) {
        logger.info("Delete Job.")
        saagieClient.getManagerStatus()
        configuration.jobs.each { job ->
            logger.error("{}", job)
            saagieClient.deleteJob(job.findId())
        }
    }
}