package io.github.adityabhaskar.dependencygraph.plugin.core

import io.github.adityabhaskar.dependencygraph.plugin.DependencyPair
import io.github.adityabhaskar.dependencygraph.plugin.ParsedGraph
import io.github.adityabhaskar.dependencygraph.plugin.asModuleProject
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import java.util.*

@Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod")
/**
 * Create a graph of all project modules, their types, dependencies and root projects.
 * @return An object of type GraphDetails containing all details
 */
internal fun parseDependencyGraph(
    rootProject: Project,
    ignoredModules: List<String>,
): ParsedGraph {
    val rootProjects = mutableListOf<Project>()
    var queue = mutableListOf(rootProject)

    // Traverse the list of all sub-folders starting with root project and add them to
    // rootProjects
    while (queue.isNotEmpty()) {
        val project = queue.removeAt(0)
        if (project.path !in ignoredModules) {
            rootProjects.add(project)
        }
        queue.addAll(project.childProjects.values)
    }

    val projects = LinkedHashSet<Project>()
    val dependencies = LinkedHashMap<DependencyPair, List<String>>()
    val multiplatformProjects = mutableListOf<Project>()
    val androidProjects = mutableListOf<Project>()
    val javaProjects = mutableListOf<Project>()

    // Again traverse the list of all sub-folders starting with the current project
    // * Add projects to project-type lists
    // * Add project dependencies to dependency hashmap with record for api/impl
    // * Add projects & their dependencies to projects list
    // * Remove any dependencies from rootProjects list
    queue = mutableListOf(rootProject)
    while (queue.isNotEmpty()) {
        val project = queue.removeAt(0)
        if (project.path in ignoredModules) {
            continue
        }
        queue.addAll(project.childProjects.values)

        if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            multiplatformProjects.add(project)
        }
        if (
            project.plugins.hasPlugin("com.android.library") ||
            project.plugins.hasPlugin("com.android.application")
        ) {
            androidProjects.add(project)
        }
        if (
            project.plugins.hasPlugin("java-library") ||
            project.plugins.hasPlugin("java") ||
            project.plugins.hasPlugin("org.jetbrains.kotlin.jvm")
        ) {
            javaProjects.add(project)
        }

        project.configurations.all { config ->
            config.dependencies
                .filterIsInstance(ProjectDependency::class.java)
                .map { it.dependencyProject }
                .filter { it.path !in ignoredModules }
                .forEach { dependency ->
                    projects.add(project)
                    projects.add(dependency)
                    if (
                        project.path !in ignoredModules &&
                        project.path != dependency.path
                    ) {
                        rootProjects.remove(dependency)
                    }

                    val graphKey =
                        DependencyPair(project.asModuleProject(), dependency.asModuleProject())
                    val traits = dependencies
                        .getOrPut(graphKey) { mutableListOf() } as MutableList

                    if (config.name.lowercase(Locale.getDefault()).endsWith("api")) {
                        traits.add("api")
                    } else {
                        traits.add("impl")
                    }
                }
        }
    }

    // Collect leaf projects which may be denoted with a different shape or rank
    val leafProjects = mutableListOf<Project>()
    projects.forEach {
        val allDependencies = it.configurations
            .map { config ->
                config.dependencies
                    .filterIsInstance(ProjectDependency::class.java)
                    .filter { dependency ->
                        dependency.dependencyProject.path != it.path
                    }
            }

        if (allDependencies.isEmpty()) {
            leafProjects.add(it)
        } else {
            leafProjects.remove(it)
        }
    }

    return ParsedGraph(
        projects = LinkedHashSet(projects.map { it.asModuleProject() }.sortedBy { it.path }),
        dependencies = dependencies,
        multiplatformProjects = multiplatformProjects.map { it.asModuleProject() },
        androidProjects = androidProjects.map { it.asModuleProject() },
        javaProjects = javaProjects.map { it.asModuleProject() },
        rootProjects = rootProjects.map { it.asModuleProject() },
    )
}