package net.c306.dependencygraph.plugin

import org.gradle.api.Project
import java.io.File

internal data class ParsedGraph(
    val projects: LinkedHashSet<ModuleProject>,
    val dependencies: LinkedHashMap<DependencyPair, List<String>>,
    val multiplatformProjects: List<ModuleProject>,
    val androidProjects: List<ModuleProject>,
    val javaProjects: List<ModuleProject>,
    val rootProjects: List<ModuleProject>,
) {
    companion object {
        // TODO: 16/06/2023 Provide via extension
        // Used for excluding module from graph
        const val SystemTestName = "system-test"
    }
}

internal data class DependencyPair(
    val origin: ModuleProject,
    val target: ModuleProject,
)

internal data class ModuleProject(
    val path: String,
    val projectDir: File,
)

internal fun Project.asModuleProject() = ModuleProject(this.path, this.projectDir)