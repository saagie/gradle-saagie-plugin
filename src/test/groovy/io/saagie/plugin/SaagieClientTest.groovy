package io.saagie.plugin

import org.junit.Test

/**
 * Created by ekoffi on 5/18/17.
 */
class SaagieClientTest {
    @Test
    void defaultValues() {
        SaagiePluginProperties saagieproperties = new SaagiePluginProperties()
        SaagieClient client = new SaagieClient(saagieproperties)
    }
}
