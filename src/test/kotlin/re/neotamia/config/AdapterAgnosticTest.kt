package re.neotamia.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.adapter.TypeAdapter
import re.neotamia.config.json.JsonSerializer
import re.neotamia.config.toml.TomlSerializer
import re.neotamia.config.yaml.YamlSerializer
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.assertEquals

class AdapterAgnosticTest {
    data class ResourceLocationDummy(val namespace: String, val path: String) {
        companion object {
            fun parse(s: String): ResourceLocationDummy {
                val ns = s.substringBefore(":")
                val p = s.substringAfter(":")
                return ResourceLocationDummy(ns, p)
            }
        }
        override fun toString(): String = "$namespace:$path"
    }

    class MyConf {
        var block: ResourceLocationDummy = ResourceLocationDummy("", "")
    }

    class ResourceLocationAdapter : TypeAdapter<ResourceLocationDummy> {
        override fun serialize(obj: ResourceLocationDummy): Any = obj.toString()
        override fun deserialize(data: Any, clazz: Class<ResourceLocationDummy>): ResourceLocationDummy {
            require(data is String) { "Expected string for ResourceLocationDummy" }
            return ResourceLocationDummy.parse(data)
        }
    }

    private fun nt(): NTConfig {
        val nt = NTConfig()
        // Register serializers with the same registry instance used by NTConfig
        nt.registerSerializer(YamlSerializer())
        nt.registerSerializer(JsonSerializer())
        nt.registerSerializer(TomlSerializer())
        nt.typeAdapterRegistry.register(ResourceLocationDummy::class.java, ResourceLocationAdapter())
        return nt
    }

    @Test
    fun yaml_scalar_adapter(@TempDir tmp: Path) {
        val file = tmp.resolve("conf.yaml")
        file.writeText("block: minecraft:stone\n")
        val cfg = nt().load(file, MyConf::class.java)
        assertEquals("minecraft", cfg.block.namespace)
        assertEquals("stone", cfg.block.path)
    }

    @Test
    fun json_scalar_adapter(@TempDir tmp: Path) {
        val file = tmp.resolve("conf.json")
        file.writeText("""{ "block": "minecraft:stone" }""")
        val cfg = nt().load(file, MyConf::class.java)
        assertEquals("minecraft", cfg.block.namespace)
        assertEquals("stone", cfg.block.path)
    }

    @Test
    fun toml_scalar_adapter(@TempDir tmp: Path) {
        val file = tmp.resolve("conf.toml")
        file.writeText("block = \"minecraft:stone\"\n")
        val cfg = nt().load(file, MyConf::class.java)
        assertEquals("minecraft", cfg.block.namespace)
        assertEquals("stone", cfg.block.path)
    }
}
