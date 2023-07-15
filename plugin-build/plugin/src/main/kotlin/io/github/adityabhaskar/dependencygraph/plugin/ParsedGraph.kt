package io.github.adityabhaskar.dependencygraph.plugin

import org.gradle.api.Project
import java.io.File

internal data class ParsedGraph(
    val projects: LinkedHashSet<ModuleProject>,
    val dependencies: LinkedHashMap<DependencyPair, List<String>>,
    val multiplatformProjects: List<ModuleProject>,
    val androidProjects: List<ModuleProject>,
    val javaProjects: List<ModuleProject>,
    val rootProjects: List<ModuleProject>,
)

internal data class DependencyPair(
    val origin: ModuleProject,
    val target: ModuleProject,
)

internal data class ModuleProject(
    val path: String,
    val projectDir: File,
)

internal fun Project.asModuleProject() = ModuleProject(this.path, this.projectDir)