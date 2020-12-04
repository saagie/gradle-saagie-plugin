package io.saagie.plugin.tasks.pipelines

import io.saagie.plugin.clients.PipelinesClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by gprevost on 10/26/20.
 * Exports all pipelines from a platform.
 */
class ImportAllPipelinesTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def importAllJobs() {
        logger.info("Import all jobs.")
        PipelinesClient pipelinesClient = new PipelinesClient(configuration)

        pipelinesClient.importPipelinesFatArchive(project.buildDir.path)
    }
}
