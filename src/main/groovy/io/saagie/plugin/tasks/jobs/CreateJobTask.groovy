package io.saagie.plugin.tasks.jobs

import groovy.json.JsonOutput
import io.saagie.plugin.JobCategory
import io.saagie.plugin.JobType
import io.saagie.plugin.SaagiePluginProperties
import io.saagie.plugin.clients.SaagieClient
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
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
        new CreateJob(configuration).createJob(logger)
    }
}

class CreateJob {
    SaagiePluginProperties configuration
    SaagieClient saagieClient

    CreateJob(SaagiePluginProperties configuration) {
        this.configuration = configuration
        saagieClient = new SaagieClient(configuration)
    }

    def createJob(Logger logger) {
        logger.info("Create Job.")
        configuration.jobs.each { job ->
            if (job.type == JobType.SQOOP && job.category != JobCategory.EXTRACT) {
                throw new UnsupportedOperationException("Can't create SQOOP job in processing category.")
            }

            if (job.type == JobType.DOCKER && job.internalPort != 0 && !job.streaming) {
                throw new UnsupportedOperationException("Can't expose internal port if not long job (streaming)")
            }

            if (job.category == JobCategory.DATAVIZ && job.type != JobType.DOCKER) {
                throw new UnsupportedOperationException("Can't create ${job.type} in smart-app section (dataviz)")
            }
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
            if (job.type != JobType.SQOOP && job.type != JobType.DOCKER) {
                current.file = saagieClient.uploadFile(Paths.get(configuration.target, configuration.fileName))
                logger.info "File: ${current.file} uploaded."
            }
            logger.info "$job.type job."
            switch (job.type) {
                case JobType.JAVA_SCALA:
                    if (job.template.empty) {
                        current.template = "java -jar {file} $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    options.language_version = job.languageVersion
                    break
                case JobType.SPARK:
                    if (job.template.empty) {
                        current.template = job.language == 'java' ? "spark-submit --class=$job.mainClass {file} $job.arguments" : "spark-submit --py-files={file} \$MESOS_SANDBOX/__main__.py $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    options.language_version = job.sparkVersion
                    options.extra_language = job.language
                    options.extra_version = job.languageVersion
                    body.streaming = job.streaming
                    break
                case JobType.PYTHON:
                    if (job.template.empty) {
                        current.template = "python {file} $job.arguments"
                    } else {
                        current.template = job.template
                    }
                    options.language_version = job.languageVersion
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
                case JobType.DOCKER:
                    current.enableAuth = job.auth
                    current.packageUrl = job.packageUrl
                    current.externalPort = job.externalPort
                    current.isExternalPort = job.externalPort != 0
                    current.externalSubDomain = job.externalSubDomain
                    current.isExternalSubDomain = !job.externalSubDomain.empty
                    if (job.streaming) {
                        current.internalPort = job.internalPort
                        current.isInternalPort = job.internalPort != 0
                        current.internalSubDomain = job.internalSubDomain
                        current.isInternalSubDomain = !job.internalSubDomain.empty
                    }
                    if (!job.dockerUser.empty && !job.dockerPassword.empty) {
                        current.authUsername = job.dockerUser
                        current.authPassword = job.dockerPassword
                        current.isDockerRegistryAuth = true
                    }
                    body.streaming = job.streaming
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
