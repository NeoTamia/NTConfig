plugins {
    id("ntconfig-build")
}

group = "re.neotamia.config.yaml"

extra["localJarRepo"] = true

dependencies {
    api(projects.modules.ntConfigCore)
    api(libs.bundles.yamlModule)
}
