plugins {
    id("ntconfig-build")
}

group = "re.neotamia.config"

extra["localJarRepo"] = true

dependencies {
    api(projects.modules.ntConfigCore)
    api(libs.bundles.jsonModule)
}
