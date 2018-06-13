package io.saagie.plugin.tasks.jobs

import io.saagie.plugin.clients.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Exports all jobs from a platform.
 * Created by ekoffi on 5/29/17.
 */
class ExportAllJobsTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def exportAllJobs() {
        logger.info("Export all jobs.")
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.exportAllArchives(project.buildDir.path)
    }
}
