package io.saagie.plugin

import io.saagie.plugin.jobs.*
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by ekoffi on 5/12/17.
 * Register all plugin's tasks in project.
 */
class Gradle implements Plugin<Project> {

    /**
     * Apply plugin to pro
     * @param project
     */
    @Override
    void apply(Project project) {
        project.extensions.create("saagie", SaagiePluginProperties)

        project.task('listJobs', type: ListJobsTask) {
            configuration = project.saagie
        }

        project.task('createJob', type: CreateJobTask) {
            configuration = project.saagie
        }

        project.task('updateJob', type: UpdateJobTask) {
            configuration = project.saagie
        }

        project.task('exportJob', type: ExportJobTask) {
            configuration = project.saagie
        }

        project.task('exportAllJobs', type: ExportAllJobsTask) {
            configuration = project.saagie
        }

        project.task('importJob', type: ImportJobTask) {
            configuration = project.saagie
        }

        project.task('importAllJobs', type: ImportAllJobsTask) {
            configuration = project.saagie
        }

        project.task('deleteJob', type: DeleteJobTask) {
            configuration = project.saagie
        }

        project.task('deleteAllJob', type: DeleteAllJobsTask) {
            configuration = project.saagie
        }
    }
}

class Server {
    String url = 'https://manager.prod.saagie.io/api/v1'
    String login = ''
    String password = ''
    String platform = ''
    String proxyHost = ''
    int proxyPort = 0
    boolean acceptSelfSigned = false
}

class Job {
    int id = 0
    String name = ''
    String type = 'java-scala'
    String category = 'extract'
    String language = 'java'
    String languageVersion = '8.121'
    String sparkVersion = '2.1.0'
    float cpu = 0.3
    int memory = 512
    int disk = 512
    boolean streaming = false
    String mainClass = ''
    String arguments = ''
    String description = ''
    String releaseNote = ''
    String email = ''
    String template = ''
}

class Packaging {
    String exportFile = ''
    String importFile = ''
    boolean currentOnly = true
}

class SaagiePluginProperties {
    Server server = new Server()
    Job job = new Job()
    Packaging packaging = new Packaging()
    String target = ''
    String fileName = ''
    boolean unsafe = false

    Object server(Closure closure) {
        server.with(closure)
    }

    Object job(Closure closure) {
        job.with(closure)
    }

    Object packaging(Closure closure) {
        packaging.with(closure)
    }
}
