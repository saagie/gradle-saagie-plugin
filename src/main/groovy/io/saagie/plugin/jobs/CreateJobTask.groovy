package io.saagie.plugin.jobs

import io.saagie.plugin.JobCategory
import io.saagie.plugin.JobType
import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

import java.nio.file.Paths

/**
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

        JSONObject options = new JSONObject()
        JSONObject current = new JSONObject()
                .put("options", options)
                .put("cpu", configuration.job.cpu)
                .put("memory", configuration.job.memory)
                .put("disk", configuration.job.disk)
                .put("releaseNote", configuration.job.releaseNote)
        JSONObject body = new JSONObject()
                .put("platform_id", configuration.platform)
                .put("capsule_code", configuration.job.type)
                .put("category", configuration.job.category)
                .put("current", current)
                .put("description", configuration.job.description)
                .put("manual", true)
                .put("name", configuration.job.name)
                .put("retry", "")
                .put("schedule", "R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S")
        if (configuration.type != JobType.SQOOP) {
            String fileName = saagieClient.uploadFile(Paths.get(configuration.target, configuration.fileName))
            current.put("file", fileName)
            logger.info("File: $fileName uploaded.")
        }
        logger.info("$configuration.job.type job.")
        switch (configuration.job.type) {
            case JobType.JAVA_SCALA:
                current
                        .put("template", "java -jar {file} $configuration.job.arguments")
                options
                        .put("language_version", configuration.job.languageVersion)
                break
            case JobType.SPARK:
                if (configuration.job.language == 'java') {
                    current
                            .put("template", "spark-submit --class=$configuration.job.mainClass {file} $configuration.job.arguments")
                } else {
                    current
                            .put("template", "spark-submit --py-files={file} \$MESOS_SANDBOX/__main__.py $configuration.job.arguments")
                }
                options
                        .put("language_version", configuration.job.sparkVersion)
                        .put("extra_language", configuration.job.language)
                        .put("extra_version", configuration.job.languageVersion)
                if (configuration.job.streaming) {
                    body.put("streaming", true)
                }
                break
            case JobType.PYTHON:
                current
                        .put("template", "python {file} $configuration.job.arguments")
                options
                        .put("language_version", configuration.job.languageVersion)
                break
            case JobType.R:
                current
                        .put("template", "Rscript {file} $configuration.job.arguments")
                break
            case JobType.TALEND:
                current
                        .put("template", "sh {file} $configuration.job.arguments")
                break
            case JobType.SQOOP:
                current
                        .put("template", configuration.job.template)
                break
            default:
                throw new UnsupportedOperationException("$configuration.job.type is currently not supported.")
        }
        logger.info(body.toString(4))
        logger.info("Id: ${saagieClient.createJob(body)}")
    }
}
