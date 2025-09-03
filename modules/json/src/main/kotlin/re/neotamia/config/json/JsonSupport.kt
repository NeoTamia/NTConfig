package re.neotamia.config.json

import kotlinx.serialization.json.Json

object JsonSupport {
    val default: Json = Json { ignoreUnknownKeys = true }
}
