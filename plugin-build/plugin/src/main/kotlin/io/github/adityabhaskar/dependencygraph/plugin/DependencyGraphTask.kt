package io.github.adityabhaskar.dependencygraph.plugin

import io.github.adityabhaskar.dependencygraph.plugin.ShowLegend.*
import io.github.adityabhaskar.dependencygraph.plugin.core.DrawConfig
import io.github.adityabhaskar.dependencygraph.plugin.core.drawDependencyGraph
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Enum to provide the direction in which the graph should flow.
 *
 * @property directionString mermaid specific string representing each direction.
 */
enum class Direction(internal val directionString: String) {
    TopToBottom(directionString = "TB"),
    BottomToTop(directionString = "BT"),
    LeftToRight(directionString = "LR"),
    RightToLeft(directionString = "RL"),
}

/**
 * Enum that provides options for when and where to add a legend to the generated graphs.
 * * [Always] will add a legend to all graphs
 * * [OnlyInRootGraph] will only add a legend to the root graph
 * * [Never] will not add a legend to any graph
 */
enum class ShowLegend {
    Always, OnlyInRootGraph, Never
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

    /** Github URL for the repository. */
    @get:Input
    @get:Option(option = "repoRootUrl", description = "Github URL for the repository")
    @get:Optional
    abstract val repoRootUrl: Property<String>

    /** Name of the main branch. `main` is used if not provided. */
    @get:Input
    @get:Option(
        option = "mainBranchName",
        description = "Name of the main branch. `main` is used if not provided.",
    )
    @get:Optional
    abstract val mainBranchName: Property<String>

    /** Name for the file where graph is saved. Default is dependency-graph.md`. */
    @get:Input
    @get:Option(
        option = "graphFileName",
        description = "Name for the file where graph is saved. Default is dependency-graph.md`",
    )
    @get:Optional
    abstract val graphFileName: Property<String>

    /**
     * The direction in which dependency graph should be laid out. Default is
     * [Direction.LeftToRight].
     **/
    @get:Input
    @get:Option(
        option = "graphDirection",
        description = "The direction in which dependency graph should be laid out. Default is LR.",
    )
    @get:Optional
    abstract val graphDirection: Property<Direction>

    /**
     * Whether module name text should link to graphs for that module.
     *
     * Github doesn't support click navigation from mermaid graphs at the moment. Linking the text
     * instead provides a work around for allowing navigating between subgraphs.
     */
    @get:Input
    @get:Option(
        option = "shouldLinkModuleText",
        description = "Whether module name text should link to graphs for that module",
    )
    @get:Optional
    abstract val shouldLinkModuleText: Property<Boolean>

    /** Whether and where a legend should be displayed. */
    @get:Input
    @get:Option(
        option = "showLegend",
        description = "Whether and where a legend should be displayed",
    )
    @get:Optional
    abstract val showLegend: Property<ShowLegend>

    /** Whether and where a legend should be displayed. */
    @get:Input
    @get:Option(
        option = "shouldGroupModules",
        description = "Whether submodules should be grouped together",
    )
    @get:Optional
    abstract val shouldGroupModules: Property<Boolean>

    /** The project dependencies graph as [ParsedGraph]`. */
    @get:Input
    @get:Option(
        option = "graphDetails",
        description = "The project dependencies graph as [ParsedGraph]",
    )
    internal abstract val parsedGraph: Property<ParsedGraph>

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
        val graph = parsedGraph.get()

        val showLegend = showLegend.getOrElse(OnlyInRootGraph)
        val directionString = graphDirection.getOrElse(Direction.LeftToRight).directionString
        val fileName = appendMarkDownIfNeeded(
            providedFileName = graphFileName.getOrElse(DEFAULT_GRAPH_FILE_NAME),
        )
        val moduleBaseUrl = createGraphUrl(
            repoUrl = repoRootUrl.orNull,
            mainBranchName = mainBranchName.orNull,
        )
        val shouldLinkModuleText = shouldLinkModuleText.getOrElse(true)
        val shouldGroupModules = shouldGroupModules.getOrElse(false)

        // Draw sub graph of dependencies and dependents for each module
//        graph.projects.forEach {
//            drawDependencyGraph(
//                currentProject = it,
//                parsedGraph = graph,
//                isRootGraph = false,
//                config = DrawConfig(
//                    rootDir = project.rootDir,
//                    moduleBaseUrl = moduleBaseUrl,
//                    showLegend = showLegend,
//                    graphDirection = directionString,
//                    fileName = fileName,
//                    shouldLinkModuleText = shouldLinkModuleText,
//        shouldGroupModules = shouldGroupModules,
//                ),
//            )
//        }

        // Draw the full graph of all modules
        drawDependencyGraph(
            currentProject = project.rootProject.asModuleProject(),
            parsedGraph = graph,
            isRootGraph = true,
            config = DrawConfig(
                rootDir = project.rootDir,
                moduleBaseUrl = moduleBaseUrl,
                showLegend = showLegend,
                graphDirection = directionString,
                fileName = fileName,
                shouldLinkModuleText = shouldLinkModuleText,
                shouldGroupModules = shouldGroupModules,
            ),
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
            "$repoUrl/blob/$branchName"
        }
    }

    private fun appendMarkDownIfNeeded(providedFileName: String) = when {
        providedFileName.isBlank() -> DEFAULT_GRAPH_FILE_NAME
        !providedFileName.endsWith(".md", ignoreCase = true) -> "$providedFileName.md"
        else -> providedFileName
    }

    companion object {
        private const val DEFAULT_BRANCH_NAME = "main"

        private const val DEFAULT_GRAPH_FILE_NAME = "dependencyGraph.md"
    }
}