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
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.getManagerStatus()

        JSONObject options = new JSONObject()
        JSONObject current = new JSONObject()
                .put("file", configuration.fileName)
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
                .put("schedule", "R0/2016-07-06T15:47:52.051Z/P0Y0M1DT0H0M0S")
        String fileName = saagieClient.uploadFile(configuration.target, configuration.fileName)
        logger.info("File: $fileName uploaded.")
        current.put("file", fileName)
        switch (configuration.type) {
            case JobType.JAVA_SCALA:
                current
                        .put("template", "java -jar {file} $configuration.arguments")
                options
                        .put("language_version", configuration.languageVersion)
                logger.info(body.toString(4))
                logger.info("Id: ${saagieClient.createJob(body)}")
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
                logger.info(body.toString(4))
                logger.info("Id: ${saagieClient.createJob(body)}")
                break
            case JobType.PYTHON:
                current
                        .put("template", "python {file} $configuration.arguments")
                options
                        .put("language_version", configuration.languageVersion)
                break
            default:
                throw new UnsupportedOperationException("$configuration.type is currently not supported.")
        }
    }
}
