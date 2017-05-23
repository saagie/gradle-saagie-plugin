package io.saagie.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

/**
 * Created by ekoffi on 5/12/17.
 */
class CreateJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def createJob() {
        logger.info("Create Job.")
        if(configuration.type == JobType.SQOOP && configuration.category == JobCategory.PROCESSING) {
            throw new UnsupportedOperationException("Can't create SQOOP job in processing category.")
        }
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.getManagerStatus()

        JSONObject options = new JSONObject()
        JSONObject current = new JSONObject()
                .put("options", options)
                .put("cpu", configuration.cpu)
                .put("memory", configuration.memory)
                .put("disk", configuration.disk)
                .put("releaseNote", configuration.releaseNote)
        JSONObject body = new JSONObject()
                .put("platform_id", configuration.platform)
                .put("capsule_code", configuration.type)
                .put("category", configuration.category)
                .put("current", current)
                .put("description", configuration.description)
                .put("manual", true)
                .put("name", configuration.name)
                .put("retry", "")
                .put("schedule", "R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S")
        if (configuration.type != JobType.SQOOP) {
            String fileName = saagieClient.uploadFile(configuration.target, configuration.fileName)
            current.put("file", fileName)
            logger.info("File: $fileName uploaded.")
        }
        logger.info("$configuration.type job.")
        switch (configuration.type) {
            case JobType.JAVA_SCALA:
                current
                        .put("template", "java -jar {file} $configuration.arguments")
                options
                        .put("language_version", configuration.languageVersion)
                break
            case JobType.SPARK:
                if (configuration.language == 'java') {
                    current
                            .put("template", "spark-submit --class=$configuration.mainClass {file} $configuration.arguments")
                } else {
                    current
                            .put("template", "spark-submit --py-files={file} \$MESOS_SANDBOX/__main__.py $configuration.arguments")
                }
                options
                        .put("language_version", configuration.sparkVersion)
                        .put("extra_language", configuration.language)
                        .put("extra_version", configuration.languageVersion)
                if (configuration.streaming) {
                    body.put("streaming", true)
                }
                break
            case JobType.PYTHON:
                current
                        .put("template", "python {file} $configuration.arguments")
                options
                        .put("language_version", configuration.languageVersion)
                break
            case JobType.R:
                current
                        .put("template", "Rscript {file} $configuration.arguments")
                break
            case JobType.TALEND:
                current
                        .put("template", "sh {file} $configuration.arguments")
                break
            case JobType.SQOOP:
                current
                        .put("template", configuration.template)
                break
            default:
                throw new UnsupportedOperationException("$configuration.type is currently not supported.")
        }
        logger.info(body.toString(4))
        logger.info("Id: ${saagieClient.createJob(body)}")
    }
}
