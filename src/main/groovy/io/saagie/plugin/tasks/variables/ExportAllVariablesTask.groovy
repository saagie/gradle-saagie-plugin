package io.saagie.plugin.tasks.variables

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

class ExportAllVariablesTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def exportAllVariables() {
        new ExportAllVariables(configuration).exportAllVariables(logger)
    }
}

class ExportAllVariables {
    SaagiePluginProperties configuration
    VariablesClient variablesClient

    ExportAllVariables(SaagiePluginProperties configuration) {
        this.configuration = configuration
        variablesClient = new VariablesClient(configuration)
    }

    def exportAllVariables(Logger logger) {
        logger.info("Import all environment variables.")
        variablesClient.exportAllVariables()
    }
}
