plugins {
    id("ntconfig-build")
}

extra["publish"] = true
dependencies {
    api(projects.modules.ntConfigCore)
    api(libs.bundles.tomlModule)
}
