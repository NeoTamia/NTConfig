plugins {
    id("ntconfig-build")
}

dependencies {
    // Only to run the jar
    implementation(kotlin("stdlib"))

    api(projects.modules.ntConfigCore)
    api(projects.modules.ntConfigJson)
    api(projects.modules.ntConfigToml)
    api(projects.modules.ntConfigYaml)

    implementation(libs.bundles.nightConfigModule)
}
