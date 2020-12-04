package io.saagie.plugin

import groovy.transform.Canonical
import io.saagie.plugin.properties.Pipeline
import io.saagie.plugin.tasks.execution.RunJobTask
import io.saagie.plugin.tasks.execution.StopJobTask
import io.saagie.plugin.tasks.jobs.*
import io.saagie.plugin.properties.Job
import io.saagie.plugin.properties.Packaging
import io.saagie.plugin.properties.Server
import io.saagie.plugin.properties.Variable
import io.saagie.plugin.tasks.variables.CreateVariableTask
import io.saagie.plugin.tasks.variables.ExportAllVariablesTask
import io.saagie.plugin.tasks.variables.ExportVariableTask
import io.saagie.plugin.tasks.variables.ImportVariableTask
import io.saagie.plugin.tasks.variables.ListVarsTask
import io.saagie.plugin.tasks.variables.UpdateVariableTask
import io.saagie.plugin.tasks.pipelines.ListPipelinesTask
import io.saagie.plugin.tasks.pipelines.ExportPipelineTask
import io.saagie.plugin.tasks.pipelines.ExportAllPipelinesTask
import io.saagie.plugin.tasks.pipelines.ImportPipelineTask
import io.saagie.plugin.tasks.pipelines.ImportAllPipelinesTask
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

        project.task('deleteAllJobs', type: DeleteAllJobsTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('createVariable', type: CreateVariableTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('updateVariable', type: UpdateVariableTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('exportVariable', type: ExportVariableTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('exportAllVariables', type: ExportAllVariablesTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('importVariable', type: ImportVariableTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('listVars', type: ListVarsTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('runJob', type: RunJobTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('stopJob', type: StopJobTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('listPipelines', type: ListPipelinesTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('exportPipeline', type: ExportPipelineTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('exportAllPipelines', type: ExportAllPipelinesTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('importPipeline', type: ImportPipelineTask) {
            group = 'saagie'
            configuration = project.saagie
        }

        project.task('importAllPipelines', type: ImportAllPipelinesTask) {
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
    List<Variable> variables = new LinkedList<Variable>()
    String target = ''
    String fileName = ''
    boolean unsafe = false
    Pipeline pipeline = new Pipeline()
    List<Pipeline> pipelines = new LinkedList<Pipeline>()

    Object server(Closure closure) {
        server.with(closure)
    }

    Object job(Closure closure) {
        job.with(closure)
    }

    Object packaging(Closure closure) {
        packaging.with(closure)
    }

    Object pipeline(Closure closure){
        pipeline.with(closure)
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

    Object variables(Closure closure) {
        variables = closure.call().collect { Closure cl ->
            def v = new Variable()
            cl.delegate = v
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
            v
        }
    }

    Object pipelines(Closure closure) {
        pipelines = closure.call().collect { Closure cl ->
            def p = new Pipeline()
            cl.delegate = p
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl.call()
            p
        }
    }
}
