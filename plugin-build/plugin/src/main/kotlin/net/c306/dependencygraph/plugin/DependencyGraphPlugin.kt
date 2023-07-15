package net.c306.dependencygraph.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "dependencyGraphConfig"
const val TASK_NAME = "dependencyGraph"

abstract class DependencyGraphPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the extension object
        val extension = project.extensions.create(
            EXTENSION_NAME,
            DependencyGraphExtension::class.java,
            project,
        )

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, DependencyGraphTask::class.java) {
            it.ignoreModules.set(extension.ignoreModules)
//            it.graphFileName
//            it.mainBranchName
//            it.repoRootUrlInput
            it.graphDetails.set(createGraph(project.rootProject))
        }
    }
}