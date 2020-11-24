package io.saagie.plugin.tasks.pipelines

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.PipelinesClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by gprevost on 10/20/20.
 * Exports all pipelines from a platform.
 */
class ExportAllPipelinesTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def exportAllPipelines() {
        logger.info("Export all pipelines.")
        PipelinesClient pipelinesClient = new PipelinesClient(configuration)

        pipelinesClient.exportAllPipelines(project.buildDir.path)
    }
}
