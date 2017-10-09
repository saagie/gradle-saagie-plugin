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
     * Apply plugin to project
     * @param project
     */
    @Override
    void apply(Project project) {
        project.extensions.create("saagie", SaagiePluginProperties)

        project.task('listJobs', type: ListJobsTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('createJob', type: CreateJobTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('updateJob', type: UpdateJobTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('exportJob', type: ExportJobTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('exportAllJobs', type: ExportAllJobsTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('importJob', type: ImportJobTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('importAllJobs', type: ImportAllJobsTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('deleteJob', type: DeleteJobTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('deleteAllJob', type: DeleteAllJobsTask) {
            group = 'saagie'
            configuration = project.saagie
        }
    }
}

@Canonical
class SaagiePluginProperties {
    Server server = new Server()
    Job job = new Job()
    List<Job> jobs = new LinkedList<Job>()
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

    Object jobs(Closure closure) {
        jobs = closure.call().collect { Closure cl ->
            def a = new Job()
            cl.delegate = a
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
            a
        }
    }
}
