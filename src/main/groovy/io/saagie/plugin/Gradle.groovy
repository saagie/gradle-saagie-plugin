package io.saagie.plugin

import groovy.transform.Canonical
import io.saagie.plugin.jobs.*
import io.saagie.plugin.properties.Job
import io.saagie.plugin.properties.Packaging
import io.saagie.plugin.properties.Server
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

@Canonical
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
