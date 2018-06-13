package io.saagie.plugin.tasks.variables

import io.saagie.plugin.clients.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

class ImportVariableTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def importVariables() {
        new ImportVariable(configuration).importVariable(logger)
    }
}

class ImportVariable {
    SaagiePluginProperties configuration
    SaagieClient saagieClient

    ImportVariable(SaagiePluginProperties configuration) {
        this.configuration = configuration
        saagieClient = new SaagieClient(configuration)
    }

    def importVariable(Logger logger) {
        logger.info("Import environment variables.")
        saagieClient.importVariables()
    }
}
