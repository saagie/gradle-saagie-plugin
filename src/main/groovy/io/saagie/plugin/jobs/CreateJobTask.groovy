package io.saagie.plugin.jobs

import groovy.json.JsonOutput
import io.saagie.plugin.JobCategory
import io.saagie.plugin.JobType
import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Paths

/**
 * Create job task.
 * Created by ekoffi on 5/12/17.
 */
class CreateJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def createJob() {
        logger.info("Create Job.")
        if (configuration.job.type == JobType.SQOOP && configuration.job.category == JobCategory.PROCESSING) {
            throw new UnsupportedOperationException("Can't create SQOOP job in processing category.")
        }
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.getManagerStatus()

        LinkedHashMap<String, Object> options = []
        def current = [
                options    : options,
                cpu        : configuration.job.cpu,
                memory     : configuration.job.memory,
                disk       : configuration.job.disk,
                releaseNote: configuration.job.releaseNote
        ]
        def body = [
                platform_id : configuration.server.platform,
                capsule_code: configuration.job.type,
                category    : configuration.job.category,
                current     : current,
                description : configuration.job.description,
                manual      : true,
                name        : configuration.job.name,
                retry       : '',
                schedule    : 'R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S'
        ]
        if (configuration.job.type != JobType.SQOOP) {
            current.file = saagieClient.uploadFile(Paths.get(configuration.target, configuration.fileName))
            logger.info "File: ${current.file} uploaded."
        }
        logger.info "$configuration.job.type job."
        switch (configuration.job.type) {
            case JobType.JAVA_SCALA:
                current.template = "java -jar {file} $configuration.job.arguments"
                options.language_version = configuration.job.languageVersion
                break
            case JobType.SPARK:
                current.template = {
                    if (configuration.job.language == 'java') {
                        return configuration.job.mainClass.empty ? "spark-submit {file} $configuration.job.arguments" : "spark-submit --class=$configuration.job.mainClass {file} $configuration.job.arguments"
                    } else {
                        return "spark-submit --py-files={file} \$MESOS_SANDBOX/__main__.py $configuration.job.arguments"
                    }
                }
                options.language_version = configuration.job.sparkVersion
                options.extra_language = configuration.job.language
                options.extra_version = configuration.job.languageVersion
                body.streaming = configuration.job.streaming
                break
            case JobType.PYTHON:
                current.template = "python {file} $configuration.job.arguments"
                options.language_version = configuration.job.languageVersion
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
        def jsonBody = JsonOutput.toJson(body)
        logger.info(JsonOutput.prettyPrint(jsonBody).stripIndent())
        logger.info("Id: ${saagieClient.createJob(jsonBody)}")
    }
}
