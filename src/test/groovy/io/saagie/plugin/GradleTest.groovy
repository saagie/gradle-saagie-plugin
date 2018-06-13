package io.saagie.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Created by ekoffi on 5/12/17.
 */
class GradleTest extends Specification {
/*    @Test
    void createJob() {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(Gradle.class)

        assertTrue(project.tasks.createJob instanceof CreateJobTask)

        assertTrue(project.tasks.updateJob instanceof UpdateJobTask)

        assertTrue(project.tasks.exportJob instanceof ExportJobTask)
    }*/

    @Rule
    final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile
    List pluginClasspath

    def "Empty job list creation 2"() {
        given:
        buildFile << """
        """
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply(Gradle.class)

        println(project.tasks.getByName('createJob'))
    }

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        pluginClasspath = getClass().classLoader.findResource('plugin-classpath.txt').readLines().collect {
            new File(it)
        }
    }
/*
    def "Empty job list creation"() {
        given:
        buildFile << """
            plugins {
                id 'io.saagie.gradle-saagie-plugin'
            }
        """

        when:
        def result = GradleRunner
                .create()
                .withProjectDir(testProjectDir.root)
                .withArguments('createJob')
                .withPluginClasspath(pluginClasspath)
                .build()

        then:
        println(result.output)
    }
    */
}
