package io.saagie.plugin.tasks.variables

import io.saagie.plugin.clients.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
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
    VariablesClient variablesClient

    ExportVariable(SaagiePluginProperties configuration) {
        this.configuration = configuration
        variablesClient = new VariablesClient(configuration)
    }

    def importVariable(Logger logger) {
        logger.info("Import environment variables.")
        variablesClient.exportVariable(configuration.variables.collect { it.id })
    }
}
