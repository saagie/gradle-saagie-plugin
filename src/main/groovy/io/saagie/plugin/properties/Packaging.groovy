package io.saagie.plugin.properties

import groovy.transform.Canonical

/**
 * Created by ekoffi on 09/24/17.
 * Packaging properties
 */
@Canonical
class Packaging {
    String exportFile = ''
    String importFile = ''
    boolean currentOnly = true
}
