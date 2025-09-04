package re.neotamia.config.annotation

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
/**
 * @param description A description of the configuration option. Output as a comment in the file if the format supports it.
 * @param name The name of the configuration option. If empty, the field or property name is used.
 * @param exclude If true, the field or property is excluded from serialization and deserialization.
 */
annotation class ConfigProperty(val description: String = "", val name: String = "", val exclude: Boolean = false)
