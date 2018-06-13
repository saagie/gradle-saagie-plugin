package io.saagie.plugin.tasks.variables

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.VariablesClient
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

/**
 * Created by ekoffi on 11/14/17.
 */
class UpdateVariableTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def updateVariable() {
        new UpdateVariable(configuration).updateVariable(logger)
    }
}

class UpdateVariable {
    SaagiePluginProperties configuration
    VariablesClient variablesClient
    JsonSlurper jsonSlurper

    UpdateVariable(SaagiePluginProperties configuration) {
        this.configuration = configuration
        this.jsonSlurper = new JsonSlurper()
        this.variablesClient = new VariablesClient(configuration)
    }

    def updateVariable(Logger logger) {
        logger.info("Update variable.")
        configuration.variables.each { variable ->
            def id = variable.id
            variablesClient.getAllVars().find {
                def var = jsonSlurper.parseText(it)
                var['id'] == id
            }
            def jsonVariable = [
                    id         : variable.id,
                    name       : variable.name,
                    value      : variable.value,
                    isPassword : variable.password,
                    platform_id: configuration.server.platform as int
            ]
            variablesClient.updateVariable(id, JsonOutput.toJson(jsonVariable))
        }
    }
}
