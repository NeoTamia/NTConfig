plugins {
    id("ntconfig-build")
}

group = "re.neotamia.config.json"

extra["localJarRepo"] = true

dependencies {
    api(projects.modules.ntConfigCore)
    api(libs.bundles.jsonModule)
}
