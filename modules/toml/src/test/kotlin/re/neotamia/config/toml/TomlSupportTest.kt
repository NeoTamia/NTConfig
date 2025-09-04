package re.neotamia.config.toml

import kotlin.test.Test
import kotlin.test.assertNotNull

class TomlSupportTest {
    @Test
    fun `default is available`() {
        assertNotNull(TomlSupport.default)
    }
}
