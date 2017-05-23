package io.saagie.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

/**
 * Created by ekoffi on 5/15/17.
 */
class UpdateJobTask extends DefaultTask {
    SaagiePluginProperties configuration

    @TaskAction
    def updateJob() {
        logger.info("Update job.")
        SaagieClient saagieClient = new SaagieClient(configuration)

        saagieClient.getManagerStatus()

        JSONObject job = saagieClient.checkJobExists()
        logger.info(job.toString(4))
        job.put("email", configuration.email)
        JSONObject current = job.getJSONObject("current")
        current
                .put("releaseNote", configuration.releaseNote)
                .put("cpu", configuration.cpu)
                .put("memory", configuration.memory)
                .put("disk", configuration.disk)
        if (configuration.type != JobType.SQOOP) {
            String fileName = saagieClient.uploadFile(configuration.target, configuration.fileName)
            current.put("file", fileName)
        }
        switch (configuration.type) {
            case JobType.JAVA_SCALA:
                current
                        .put("template", "java -jar {file} $configuration.arguments")
                current.getJSONObject("options")
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
                current.getJSONObject("options")
                        .put("language_version", configuration.sparkVersion)
                        .put("extra_language", configuration.language)
                        .put("extra_version", configuration.languageVersion)

                break
            case JobType.PYTHON:
                current
                        .put("template", "python {file} $configuration.arguments")
                current.getJSONObject("options")
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

        saagieClient.updateJob(job)
    }
}
