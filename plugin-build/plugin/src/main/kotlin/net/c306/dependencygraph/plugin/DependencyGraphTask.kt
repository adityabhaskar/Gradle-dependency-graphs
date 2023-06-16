package net.c306.dependencygraph.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Creates mermaid graphs for all modules in the app and places each graph within the module's folder.
 * An app-wide graph is also created and added to the project's root directory.
 *
 *
 * Derived from https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle
 *
 *
 * The key differences are:
 * 1. Output is in mermaidjs format to support auto display on githib
 * 2. Graphs are also generated for every module and placed in their root directory
 * 3. Module graphs also show other modules directly dependent on that module (using dashed lines)
 * 4. API dependencies are displayed with the text "API" on the connector
 * 5. Direct dependencies are connected using a bold line
 * 6. Indirect dependencies have thin lines as connectors
 * 7. Java/Kotlin modules used a hexagon for a shape, except when they are the root module in the graph
 *    * These nodes are filled with a Pink-ish colour
 * 8. Android and multiplatform modules used a rounded shape, except when they are the root module in the graph
 *    * Android nodes are filled with a Green colour
 *    * MPP nodes are filled with an Orange-ish colour
 * 9. Provided but unsupported on Github - click navigation
 *    * Module nodes are clickable, clicking through to the graph of the respective module
 */
abstract class DependencyGraphTask : DefaultTask() {

    init {
        description = "Generates dependency graph files for all local modules in the project."

        // Don't forget to set the group here.
        group = BasePlugin.BUILD_GROUP
    }

    @get:Input
    @get:Option(option = "message", description = "A message to be printed in the output file")
    abstract val message: Property<String>

    @get:Input
    @get:Option(option = "tag", description = "A Tag to be used for debug and in the output file")
    @get:Optional
    abstract val tag: Property<String>

//    @get:OutputFile
//    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun createDependencyGraph() {
        // Create graph of all dependencies
        val graph = createGraph(project.rootProject)

        // For each module, draw its sub graph of dependencies and dependents
        graph.projects.forEach { drawDependencies(it, graph, false, project.rootDir) }

        // Draw the full graph of all modules
        drawDependencies(project.rootProject, graph, true, project.rootDir)


//        // TODO: 16/06/2023 Copy code from dependency graph plugin here
//        val prettyTag = tag.orNull?.let { "[$it]" } ?: ""
//
//        logger.lifecycle("$prettyTag message is: ${message.orNull}")
//        logger.lifecycle("$prettyTag tag is: ${tag.orNull}")
//        logger.lifecycle("$prettyTag outputFile is: ${outputFile.orNull}")
//
//        outputFile.get().asFile.writeText("$prettyTag ${message.get()}")
    }
}
