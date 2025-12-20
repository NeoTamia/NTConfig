plugins {
    id("ntconfig-build")
}

dependencies {
    api(projects.modules.ntConfigCore)
    api(projects.modules.ntConfigJson)
    api(projects.modules.ntConfigToml)
    api(projects.modules.ntConfigYaml)

    implementation(libs.bundles.nightConfigModule)
}
