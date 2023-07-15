package net.c306.dependencygraph.plugin

import net.c306.dependencygraph.plugin.core.parseDependencyGraph
import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "dependencyGraphConfig"
const val TASK_NAME = "dependencyGraph"

abstract class DependencyGraphPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the extension object
        val extension = project.extensions.create(
            /* name = */ EXTENSION_NAME,
            /* type = */ DependencyGraphExtension::class.java,
            /* ...constructionArguments = */ project,
        )

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, DependencyGraphTask::class.java) {
            it.ignoreModules.set(extension.ignoreModules)
            it.graphFileName.set(extension.graphFileName)
            it.mainBranchName.set(extension.mainBranchName)
            it.repoRootUrl.set(extension.repoRootUrl)
            it.graphDirection.set(extension.graphDirection)
            it.showLegend.set(extension.showLegend)
            it.graphDetails.set(parseDependencyGraph(project.rootProject))
        }
    }
}