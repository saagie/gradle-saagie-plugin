package io.saagie.plugin.jobs

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.saagie.plugin.JobType
import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

/**
 * Created by ekoffi on 5/15/17.
 */
class UpdateJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def updateJob() {
        new UpdateJob(configuration).updateJob(logger)
    }
}

class UpdateJob {
    SaagiePluginProperties configuration
    SaagieClient saagieClient
    JsonSlurper jsonSlurper

    UpdateJob(SaagiePluginProperties configuration) {
        this.configuration = configuration
        this.jsonSlurper = new JsonSlurper()
    }

    def updateJob(Logger logger) {
        logger.info("Update job.")
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.getManagerStatus()
        configuration.jobs.each { job ->
            def id = job.findId()
            String platformJob = saagieClient.getJob(id)
            logger.info("{}", JsonOutput.prettyPrint(platformJob).stripIndent())

            def jsonMap = jsonSlurper.parseText platformJob
            jsonMap = jsonMap.findAll { k, v -> k == "current" || k == "email" }
            jsonMap.email = job.email
            def current = jsonMap.current
            current.releaseNote = job.releaseNote
            current.description = job.description
            current.cpu = job.cpu
            current.memory = job.memory
            current.disk = job.disk

            if (job.type != JobType.SQOOP) {
                current.file = saagieClient.uploadFile(Paths.get(configuration.target, configuration.fileName))
            }
            switch (job.type) {
                case JobType.JAVA_SCALA:
                    if (job.template.empty) {
                        current.template = "java -jar {file} $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    current.options.language_version = job.languageVersion
                    break
                case JobType.SPARK:
                    if (job.template.empty) {
                        current.template = job.language == 'java' ? "spark-submit --class=$job.mainClass {file} $job.arguments" : "spark-submit --py-files={file} \$MESOS_SANDBOX/__main__.py $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    current.options.language_version = job.sparkVersion
                    current.options.extra_language = job.language
                    current.options.extra_version = job.languageVersion
                    break
                case JobType.PYTHON:
                    if (job.template.empty) {
                        current.template = "python {file} $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    current.options.language_version = job.languageVersion
                    break
                case JobType.R:
                    if (job.template.empty) {
                        current.template = "Rscript {file} $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    break
                case JobType.TALEND:
                    if (job.template.empty) {
                        current.template = "sh {file} $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    break
                case JobType.SQOOP:
                    current.template = job.template
                    break
                default:
                    throw new UnsupportedOperationException("$job.type is currently not supported.")
            }
            saagieClient.updateJob(id, JsonOutput.toJson(jsonMap))
        }
    }
}
