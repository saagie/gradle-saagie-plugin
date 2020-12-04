package io.saagie.plugin.tasks.pipelines

import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.PipelinesClient
import io.saagie.plugin.clients.SaagieClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.zip.ZipFile

/**
 * Created by gprevost on 10/12/20.
 * Imports pipeline from an archive to a platform.
 */
class ImportPipelineTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def importPipeline() {
        logger.info("Import pipeline.")
        PipelinesClient pipelinesClient = new PipelinesClient(configuration)
        SaagieClient saagieClient = new SaagieClient(configuration)

        def zip = new ZipFile(new File(configuration.target, configuration.packaging.importFile))
        logger.info("importFile: {}/{}", configuration.target, configuration.packaging.importFile)

        def pipelinesInfo = ""
        def jobsInfo = ""

        if (configuration.packaging.pipelineImportErase){
            pipelinesInfo = pipelinesClient.getAllPipelinesData()
            jobsInfo = saagieClient.getAllJobsData()
        }

        pipelinesClient.importPipeline(project.buildDir.path, zip, pipelinesInfo, jobsInfo)
    }
}
