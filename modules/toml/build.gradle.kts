plugins {
    id("ntconfig-build")
}

extra["publish"] = true
dependencies {
    api(projects.modules.core)
    api(libs.bundles.tomlModule)
}
