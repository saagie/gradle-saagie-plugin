package io.saagie.plugin

import io.saagie.plugin.jobs.CreateJobTask
import io.saagie.plugin.jobs.ExportJobTask
import io.saagie.plugin.jobs.UpdateJobTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertTrue

/**
 * Created by ekoffi on 5/12/17.
 */
class GradleTest {
    @Test
    void createJob() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(Gradle.class)

        assertTrue(project.tasks.createJob instanceof CreateJobTask)

        assertTrue(project.tasks.updateJob instanceof UpdateJobTask)

        assertTrue(project.tasks.exportJob instanceof ExportJobTask)
    }
}
