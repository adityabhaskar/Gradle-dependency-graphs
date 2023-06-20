package net.c306.dependencydiagram.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "projectDependencyGraphConfig"
const val TASK_NAME = "projectDependencyGraph"

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
            it.tag.set(extension.tag)
            it.message.set(extension.message)
//            it.outputFile.set(extension.outputFile)
        }
    }
}