package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ImportVariableTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def importVariables() {
        logger.info('Import environment variables.')
        def saagieClient = new SaagieClient(configuration)
        saagieClient.importVariable(configuration.variables.collect { it.id })
    }
}
