package io.saagie.plugin.properties

import groovy.transform.Canonical

/**
 * Created by ekoffi on 09/24/17.
 * Job's properties
 */
@Canonical
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
    String idFile = ''
}
