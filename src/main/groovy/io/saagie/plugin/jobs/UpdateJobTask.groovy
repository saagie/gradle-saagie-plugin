package io.saagie.plugin.jobs

import io.saagie.plugin.JobType
import io.saagie.plugin.SaagieClient
import io.saagie.plugin.SaagiePluginProperties
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.json.JSONObject

import java.nio.file.Paths

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
        job.put("email", configuration.job.email)
        JSONObject current = job.getJSONObject("current")
        current
                .put("releaseNote", configuration.job.releaseNote)
                .put("cpu", configuration.job.cpu)
                .put("memory", configuration.job.memory)
                .put("disk", configuration.job.disk)
        if (configuration.job.type != JobType.SQOOP) {
            String fileName = saagieClient.uploadFile(Paths.get(configuration.target, configuration.fileName))
            current.put("file", fileName)
        }
        switch (configuration.job.type) {
            case JobType.JAVA_SCALA:
                current
                        .put("template", "java -jar {file} $configuration.job.arguments")
                current.getJSONObject("options")
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
                current.getJSONObject("options")
                        .put("language_version", configuration.job.sparkVersion)
                        .put("extra_language", configuration.job.language)
                        .put("extra_version", configuration.job.languageVersion)

                break
            case JobType.PYTHON:
                current
                        .put("template", "python {file} $configuration.job.arguments")
                current.getJSONObject("options")
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

        saagieClient.updateJob(job)
    }
}
