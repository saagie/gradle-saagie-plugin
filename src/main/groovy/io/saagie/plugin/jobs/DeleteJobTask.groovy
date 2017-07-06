package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
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
        logger.info("Delete Job.")
        SaagieClient saagieClient = new SaagieClient(configuration)
        saagieClient.getManagerStatus()
        saagieClient.deleteJob()
    }
}
