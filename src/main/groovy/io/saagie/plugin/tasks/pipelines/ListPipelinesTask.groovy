package io.saagie.plugin.tasks.pipelines

import io.saagie.plugin.clients.PipelinesClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

/**
 * List all pipeline on platform
 * Created by gprevost on 10/05/20.
 */
class ListPipelinesTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def listPipelines() {
        new ListPipelines(configuration).listPipelines(logger)
    }
}

class ListPipelines {
    SaagiePluginProperties configuration
    PipelinesClient pipelinesClient

    ListPipelines(SaagiePluginProperties configuration) {
        this.configuration = configuration
        this.pipelinesClient = new PipelinesClient(configuration)
    }

    def listPipelines(Logger logger) {
        logger.info("List pipelines.")
        logger.quiet("Pipeline list: {}", pipelinesClient.allPipelines)
    }
}