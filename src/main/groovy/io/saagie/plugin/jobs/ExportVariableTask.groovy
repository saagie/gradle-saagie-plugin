package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

class ExportVariableTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def exportVariable() {
        new ExportVariable(configuration).importVariable(logger)
    }
}

class ExportVariable {
    SaagiePluginProperties configuration
    SaagieClient saagieClient

    ExportVariable(SaagiePluginProperties configuration) {
        this.configuration = configuration
        saagieClient = new SaagieClient(configuration)
    }

    def importVariable(Logger logger) {
        logger.info("Import environment variables.")
        saagieClient.exportVariable(configuration.variables.collect { it.id })
    }
}
