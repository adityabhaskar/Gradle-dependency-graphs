package net.c306.dependencydiagram.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "projectDependencyDiagramConfig"
const val TASK_NAME = "projectDependencyDiagram"

abstract class DependencyDiagramPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the extension object
        val extension = project.extensions.create(
            EXTENSION_NAME,
            DependencyDiagramExtension::class.java,
            project,
        )

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, DependencyDiagramTask::class.java) {
            it.ignoreModules.set(extension.ignoreModules)
            it.graphFileName
            it.mainBranchName
            it.repoRootUrlInput
            it.graphDetails.set(createGraph(project.rootProject))
        }
    }
}