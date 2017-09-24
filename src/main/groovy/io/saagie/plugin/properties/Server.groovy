package io.saagie.plugin.properties

import groovy.transform.Canonical

/**
 * Created by ekoffi on 09/24/17.
 * Platform properties
 */
@Canonical
class Server {
    String url = 'https://manager.prod.saagie.io/api/v1'
    String login = ''
    String password = ''
    String platform = ''
    String proxyHost = ''
    int proxyPort = 0
    boolean acceptSelfSigned = false
}
