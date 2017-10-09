package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by ekoffi on 5/29/17.
 */
class ExportJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def exportJob() {
        logger.info("Export job.")
        SaagieClient saagieClient = new SaagieClient(configuration)
        configuration.jobs.each {
            saagieClient.exportArchive(it.findId(), project.buildDir.path)
        }
    }
}
