package re.neotamia.config.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonSupportTest {
    @Serializable
    data class Foo(val a: Int)

    @Test
    fun `default ignores unknown keys`() {

    }
}
