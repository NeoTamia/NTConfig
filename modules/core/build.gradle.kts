plugins {
    id("ntconfig-build")
}

extra["publish"] = true

dependencies {
    api(libs.nightConfigCore)

    testImplementation(libs.nightConfigJson)
    testImplementation(libs.nightConfigYaml)
    testImplementation(libs.nightConfigToml)
}
