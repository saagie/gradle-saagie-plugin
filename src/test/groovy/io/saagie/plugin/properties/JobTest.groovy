package io.saagie.plugin.properties

import spock.lang.Specification

class JobTest extends Specification {
    def "Empty id"() {
        given:
        def job = new Job()

        when:
        def id = job.findId()

        then:
        id == 0
    }

    def "Find id in variable"() {
        given:
        def job = new Job()
        job.id = 666

        when:
        def id = job.findId()

        then:
        id == 666
    }

    def "Find id in file"() {
        given:
        def job = new Job()
        job.idFile = new File(this.getClass().getResource('/file.id').path).absolutePath

        when:
        def id = job.findId()

        then:
        id == 666
    }

    def "Override id in file"() {
        given:
        def job = new Job()
        job.idFile = new File(this.getClass().getResource('/file.id').path).absolutePath
        job.id = 667

        when:
        def id = job.findId()

        then:
        id == 667
    }
}
