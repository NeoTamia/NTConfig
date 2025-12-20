plugins {
    id("ntconfig-build")
}

group = "re.neotamia.config.toml"

extra["localJarRepo"] = true

dependencies {
    api(projects.modules.ntConfigCore)
    api(libs.bundles.tomlModule)
}
