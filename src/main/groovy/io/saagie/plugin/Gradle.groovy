package io.saagie.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by ekoffi on 5/12/17.
 */
class Gradle implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("saagie", SaagiePluginProperties)
        project.task('createJob', type: CreateJobTask) {
            configuration = project.saagie
        }

        project.task('updateJob', type: UpdateJobTask) {
            configuration = project.saagie
        }
    }
}

class SaagiePluginProperties {
    String url = 'https://manager.prod.saagie.io/api/v1'
    String login = ''
    String password = ''
    String platform = ''
    int job = 0
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
    String target = ''
    String fileName = ''
    String mainClass = ''
    String arguments = ''
    String description = ''
    String releaseNote = ''
    String email = ''
}
