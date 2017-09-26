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
        configuration.jobs.each { job ->
            if (job.type == JobType.SQOOP && job.category == JobCategory.PROCESSING) {
                throw new UnsupportedOperationException("Can't create SQOOP job in processing category.")
            }
            SaagieClient saagieClient = new SaagieClient(configuration)

            saagieClient.getManagerStatus()

            LinkedHashMap<String, Object> options = []
            def current = [
                    options    : options,
                    cpu        : job.cpu,
                    memory     : job.memory,
                    disk       : job.disk,
                    releaseNote: job.releaseNote
            ]
            def body = [
                    platform_id : configuration.server.platform,
                    capsule_code: job.type,
                    category    : job.category,
                    current     : current,
                    description : job.description,
                    manual      : true,
                    name        : job.name,
                    retry       : '',
                    schedule    : 'R0/2017-05-23T13:59:05.587Z/P0Y0M1DT0H0M0S'
            ]
            if (job.type != JobType.SQOOP) {
                current.file = saagieClient.uploadFile(Paths.get(configuration.target, configuration.fileName))
                logger.info "File: ${current.file} uploaded."
            }
            logger.info "$job.type job."
            switch (job.type) {
                case JobType.JAVA_SCALA:
                    current.template = "java -jar {file} $job.arguments"
                    options.language_version = job.languageVersion
                    break
                case JobType.SPARK:
                    current.template = {
                        if (job.language == 'java') {
                            job.mainClass.empty ? "spark-submit {file} $job.arguments" : "spark-submit --class=$job.mainClass {file} $job.arguments"
                        } else {
                            "spark-submit --py-files={file} \$MESOS_SANDBOX/__main__.py $job.arguments"
                        }
                    }.call()
                    options.language_version = job.sparkVersion
                    options.extra_language = job.language
                    options.extra_version = job.languageVersion
                    body.streaming = job.streaming
                    break
                case JobType.PYTHON:
                    current.template = "python {file} $job.arguments"
                    options.language_version = job.languageVersion
                    break
                case JobType.R:
                    current.template = "Rscript {file} $job.arguments"
                    break
                case JobType.TALEND:
                    current.template = "sh {file} $job.arguments"
                    break
                case JobType.SQOOP:
                    current.template = job.template
                    break
                default:
                    throw new UnsupportedOperationException("$job.type is currently not supported.")
            }
            def jsonBody = JsonOutput.toJson(body)
            logger.info(JsonOutput.prettyPrint(jsonBody).stripIndent())
            def jobId = saagieClient.createJob(jsonBody)
            if (!job.idFile.empty) {
                new File(job.idFile).text = jobId
            }
            logger.info("Id: ${jobId}")
        }
    }
}
