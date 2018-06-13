package io.saagie.plugin.tasks.jobs

import io.saagie.plugin.clients.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by ekoffi on 5/30/17.
 */
class ImportJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def importJob() {
        logger.info("Import job.")
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.importArchive(project.buildDir.path)
    }
}
