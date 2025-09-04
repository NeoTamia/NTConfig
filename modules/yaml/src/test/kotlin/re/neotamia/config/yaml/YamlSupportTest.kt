package re.neotamia.config.yaml

import kotlin.test.Test
import kotlin.test.assertNotNull

class YamlSupportTest {
    @Test
    fun `default is available`() {
        assertNotNull(YamlSupport.default)
    }
}
