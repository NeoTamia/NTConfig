plugins {
    id("ntconfig-build")
}

group = "re.neotamia.config"

dependencies {
    api(libs.nightConfigCore)

    testImplementation(libs.nightConfigJson)
    testImplementation(libs.nightConfigYaml)
    testImplementation(libs.nightConfigToml)
}
