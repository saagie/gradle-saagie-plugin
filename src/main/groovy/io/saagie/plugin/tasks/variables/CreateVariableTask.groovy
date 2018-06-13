package io.saagie.plugin.tasks.variables

import groovy.json.JsonOutput
import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

/**
 * Create variable task.
 * Created by ekoffi on 5/12/17.
 */
class CreateVariableTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def createVariable() {
        new CreateVariable(configuration).createVariable(logger)
    }
}

class CreateVariable {
    SaagiePluginProperties configuration
    VariablesClient variablesClient

    CreateVariable(SaagiePluginProperties configuration) {
        this.configuration = configuration
        variablesClient = new VariablesClient(configuration)
    }

    def createVariable(Logger logger) {
        logger.info("Create variable.")
        configuration.variables.each { variable ->
            def mapVariable = [
                    name       : variable.name,
                    value      : variable.value,
                    isPassword : variable.password,
                    platform_id: configuration.server.platform as int
            ]
            def jsonVariable = JsonOutput.toJson(mapVariable)
            variablesClient.createVariable(jsonVariable)
        }
    }
}
