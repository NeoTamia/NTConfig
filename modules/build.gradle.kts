dependencies {
    rootProject.subprojects.filter { it.path.startsWith(":modules:") }.forEach { subproject ->
        api(project(subproject.path))
    }
}
