package io.saagie.plugin.jobs

import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ListVarsTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def listVars() {
        logger.info("List environment variables.")
        def saagieClient = new SaagieClient(configuration)
        logger.quiet("Var lists: {}", saagieClient.allVars)
    }
}
