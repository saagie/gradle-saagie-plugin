package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

class ExportAllVariablesTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def exportAllVariables() {
        new ExportAllVariables(configuration).importVariable(logger)
    }
}

class ExportAllVariables {
    SaagiePluginProperties configuration
    SaagieClient saagieClient

    ExportAllVariables(SaagiePluginProperties configuration) {
        this.configuration = configuration
        saagieClient = new SaagieClient(configuration)
    }

    def importVariable(Logger logger) {
        logger.info("Import all environment variables.")
        saagieClient.exportAllVariables()
    }
}
