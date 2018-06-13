package io.saagie.plugin.tasks.variables

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

class ListVarsTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def listVars() {
        new ListVars(configuration).listVars(logger)
    }
}

class ListVars {
    SaagiePluginProperties configuration
    VariablesClient variablesClient

    ListVars(SaagiePluginProperties configuration) {
        this.configuration = configuration
        this.variablesClient = new VariablesClient(configuration)
    }

    def listVars(Logger logger) {
        logger.info("List environment variables.")
        logger.quiet("Var lists: {}", variablesClient.allVars)
    }

}