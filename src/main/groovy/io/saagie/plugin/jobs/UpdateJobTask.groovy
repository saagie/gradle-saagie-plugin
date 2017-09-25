package io.saagie.plugin.jobs

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.saagie.plugin.JobType
import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

/**
 * Created by ekoffi on 5/15/17.
 */
class UpdateJobTask extends DefaultTask {
    SaagiePluginProperties configuration
    JsonSlurper jsonSlurper = new JsonSlurper()

    @TaskAction
    def updateJob() {
        logger.info("Update job.")
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.getManagerStatus()
        def id = 0
        if (configuration.job.id != 0) {
            id = configuration.job.id
        } else if (!configuration.job.idFile.empty) {
            id = new File(configuration.job.idFile).text.toInteger()
        }
        String job = saagieClient.getJob(id)
        logger.info(JsonOutput.prettyPrint(job).stripIndent())

        def jsonMap = jsonSlurper.parseText job
        jsonMap = jsonMap.findAll {k, v -> k == "current" || k == "email"}
        jsonMap.email = configuration.job.email
        def current = jsonMap.current
        current.releaseNote = configuration.job.releaseNote
        current.cpu = configuration.job.cpu
        current.memory = configuration.job.memory
        current.disk = configuration.job.disk

        if (configuration.job.type != JobType.SQOOP) {
            current.file = saagieClient.uploadFile(Paths.get(configuration.target, configuration.fileName))
        }
        switch (configuration.job.type) {
            case JobType.JAVA_SCALA:
                current.template = "java -jar {file} $configuration.job.arguments"
                current.options.language_version = configuration.job.languageVersion
                break
            case JobType.SPARK:
                current.template = configuration.job.language == 'java' ? "spark-submit --class=$configuration.job.mainClass {file} $configuration.job.arguments" : "spark-submit --py-files={file} \$MESOS_SANDBOX/__main__.py $configuration.job.arguments"
                current.options.language_version = configuration.job.sparkVersion
                current.options.extra_language = configuration.job.language
                current.options.extra_version = configuration.job.languageVersion
                break
            case JobType.PYTHON:
                current.template = "python {file} $configuration.job.arguments"
                current.options.language_version = configuration.job.languageVersion
                break
            case JobType.R:
                current.template = "Rscript {file} $configuration.job.arguments"
                break
            case JobType.TALEND:
                current.template = "sh {file} $configuration.job.arguments"
                break
            case JobType.SQOOP:
                current.template = configuration.job.template
                break
            default:
                throw new UnsupportedOperationException("$configuration.job.type is currently not supported.")
        }
        saagieClient.updateJob(id, JsonOutput.toJson(jsonMap))
    }
}
