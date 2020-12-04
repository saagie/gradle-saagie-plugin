package io.saagie.plugin.properties

import groovy.transform.Canonical

/**
 * Created by gprevost on 10/06/20.
 * Pipeline's properties
 */
@Canonical
class Pipeline {
    int id = 0
    String name = "Generic pipeline"
    String schedule = ""
    List<Job> listJob = null

    String idFile = ''

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