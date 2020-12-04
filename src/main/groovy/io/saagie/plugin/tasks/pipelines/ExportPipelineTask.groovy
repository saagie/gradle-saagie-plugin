package io.saagie.plugin.tasks.pipelines

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.PipelinesClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by gprevost on 10/06/20.
 */
class ExportPipelineTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def exportPipeline() {
        logger.info("Export pipeline.")
        PipelinesClient pipelinesClient = new PipelinesClient(configuration)

        configuration.pipelines.each {
            pipelinesClient.exportPipeline(it.findId(), project.buildDir.path, configuration.target)
        }
    }
}
