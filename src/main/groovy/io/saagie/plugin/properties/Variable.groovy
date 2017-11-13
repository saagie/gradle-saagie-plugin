package io.saagie.plugin.properties

import groovy.transform.Canonical

/**
 * Created by ekoffi on 09/24/17.
 */
@Canonical
class Variable {
    int id = 0
    String name = ''
    String value = ''
    boolean password = false
}
