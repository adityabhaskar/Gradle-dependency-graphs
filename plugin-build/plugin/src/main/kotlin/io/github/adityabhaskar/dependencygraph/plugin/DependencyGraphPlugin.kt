package io.github.adityabhaskar.dependencygraph.plugin

import io.github.adityabhaskar.dependencygraph.plugin.core.parseDependencyGraph
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UndocumentedPublicClass")
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
            it.graphFileName.set(extension.graphFileName)
            it.mainBranchName.set(extension.mainBranchName)
            it.repoRootUrl.set(extension.repoRootUrl)
            it.graphDirection.set(extension.graphDirection)
            it.showLegend.set(extension.showLegend)
            it.shouldLinkModuleText.set(extension.shouldLinkModuleText)

            it.parsedGraph.set(
                parseDependencyGraph(
                    rootProject = project.rootProject,
                    ignoredModules = extension.ignoreModules.orNull ?: emptyList(),
                ),
            )
        }
    }

    companion object {
        private const val EXTENSION_NAME = "dependencyGraphConfig"
        private const val TASK_NAME = "dependencyGraph"
    }
}