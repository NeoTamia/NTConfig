plugins {
    id("ntconfig-build")
}

dependencies {
    // Only to run the jar
    implementation(kotlin("stdlib"))

    api(projects.modules.core)
    api(projects.modules.json)
    api(projects.modules.toml)
    api(projects.modules.yaml)

    implementation(libs.bundles.nightConfigModule)
}
