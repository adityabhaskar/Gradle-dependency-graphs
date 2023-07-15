package net.c306.dependencygraph.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

enum class Direction {
    TopToBottom, BottomToTop, LeftToRight, RightToLeft
}

/**
 * Creates mermaid graphs for all modules in the app and places each graph within the module's folder.
 * An app-wide graph is also created and added to the project's root directory.
 *
 *
 * [Derived from JakeWharton/SdkSearch](https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle)
 *
 *
 * Key differences are:
 * 1. Output is in mermaid-js format to support auto display on github
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
        group = BasePlugin.BUILD_GROUP
        description = "Generates dependency graph files for all local modules in the project."
    }

    @get:Input
    @get:Option(
        option = "ignoreModules",
        description = "Full paths, e.g. ':live:test-ui` of the modules you may want to ignore",
    )
    @get:Optional
    abstract val ignoreModules: ListProperty<String>

    @get:Input
    @get:Option(option = "repoRootUrl", description = "Github URL for the repository")
    @get:Optional
    abstract val repoRootUrl: Property<String>

    @get:Input
    @get:Option(
        option = "mainBranchName",
        description = "Name of the main branch. `main` is used if not provided.",
    )
    @get:Optional
    abstract val mainBranchName: Property<String>

    @get:Input
    @get:Option(
        option = "graphFileName",
        description = "Name for the file where graph is saved. Default is dependency-graph.md`",
    )
    @get:Optional
    abstract val graphFileName: Property<String>

    @get:Input
    @get:Option(
        option = "graphDirection",
        description = "The direction in which dependency graph should be laid out. Default is LR.",
    )
    @get:Optional
    abstract val graphDirection: Property<Direction>

    @get:Input
    @get:Option(
        option = "graphDetails",
        description = "The project dependencies graph as [GraphDetails]",
    )
    internal abstract val graphDetails: Property<GraphDetails>

    /**
     * Creates mermaid graphs for all modules in the app and places each graph within the module's
     * folder. An app-wide graph is also created and added to the project's root directory.
     *
     *
     * [Derived from JakeWharton/SdkSearch](https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle)
     *
     *
     * Key differences are:
     * 1. Output is in mermaid-js format to support auto display on github
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
    @TaskAction
    fun createDependencyGraph() {

        // Create graph of all dependencies
        val graph = graphDetails.get()

        val moduleBaseUrl = createGraphUrl(
            repoUrl = repoRootUrl.orNull,
            mainBranchName = mainBranchName.orNull,
        )

        // Draw sub graph of dependencies and dependents for each module
        graph.projects.forEach {
            drawDependencies(
                currentProject = it,
                graphDetails = graph,
                isRootGraph = false,
                rootDir = project.rootDir,
                moduleBaseUrl = moduleBaseUrl,
            )
        }

        // Draw the full graph of all modules
        drawDependencies(
            currentProject = project.rootProject.asModuleProject(),
            graphDetails = graph,
            isRootGraph = true,
            rootDir = project.rootDir,
            moduleBaseUrl = moduleBaseUrl,
        )
    }

    private fun createGraphUrl(repoUrl: String?, mainBranchName: String?): String? {
        if (repoUrl == null) {
            return null
        }

        val branchName = mainBranchName ?: DEFAULT_BRANCH_NAME

        return if (repoUrl.endsWith('/')) {
            "${repoUrl}blob/$branchName"
        } else {
            "${repoUrl}/blob/$branchName"
        }
    }

    companion object {
        private const val DEFAULT_BRANCH_NAME = "main"
    }
}