package io.saagie.plugin.properties

import groovy.transform.Canonical

/**
 * Created by ekoffi on 09/24/17.
 * Job's properties
 */
@Canonical
class Job {
    int id = 0
    String name = 'Generic Job Name'
    String type = 'java-scala'
    String category = 'extract'
    String language = 'java'
    String languageVersion = '8.131'
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
    String idFile = ''
    //TODO: always_email, important versions

    /**
     * Find the job id from configuration or file.
     * @return The id found. Configuration id supersedes file's one.
     */
    int findId() {
        int res = id
        if (id == 0 && !idFile.empty) {
            res = new File(idFile).text.toInteger()
        }
        return res
    }
}
