package io.saagie.plugin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
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
    }

    @Test
    void saagiePropertiesDefault() {
        Map defaultValues = [
                'url'            : 'https://manager.prod.saagie.io/api/v1',
                'login'          : '',
                'password'       : '',
                'platform'       : '',
                'job'            : 0,
                'name'           : '',
                'type'           : 'java-scala',
                'category'       : 'extract',
                'language'       : 'java',
                'languageVersion': '8.121',
                'sparkVersion'   : '2.1.0',
                'cpu'            : 0.3,
                'memory'         : 512,
                'disk'           : 512,
                'streaming'      : false,
                'target'         : '',
                'fileName'       : '',
                'mainClass'      : '',
                'arguments'      : '',
                'description'    : '',
                'releaseNote'    : '',
                'email'          : ''
        ]
        SaagiePluginProperties saagieproperties = new SaagiePluginProperties()
        saagieproperties.properties.each {
            if (it.key != 'class') {
                if (it.value instanceof Float) {
                    assertEquals((float) defaultValues[it.key], (float) it.value, 0.001f)
                } else {
                    assertEquals(defaultValues[it.key], it.value)
                }
            }
        }
    }
}
