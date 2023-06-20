package net.c306.dependencydiagram.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.util.*

/**
 * Creates mermaid graphs for all modules in the app and places each graph within the module's folder.
 * An app-wide graph is also created and added to the project's root directory.
 *
 *
 * [Derived from JakeWharton/SdkSearch](https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle)
 *
 *
 * Key differences are:
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

    private data class DependencyPair(
        val origin: Project,
        val target: Project,
    )

    private data class GraphDetails(
        val projects: LinkedHashSet<Project>,
        val dependencies: LinkedHashMap<DependencyPair, List<String>>,
        val multiplatformProjects: List<Project>,
        val androidProjects: List<Project>,
        val javaProjects: List<Project>,
        val rootProjects: List<Project>,
    ) {
        companion object {
            // TODO: 16/06/2023 Provide via extension
            // Used for excluding module from graph
            const val SystemTestName = "system-test"

            // TODO: 16/06/2023 Provide via extension
            // Used for linking module nodes to their graphs
            const val RepoPath = "https://github.com/oorjalabs/todotxt-for-android/blob/main"

            // TODO: 16/06/2023 Provide via extension
            const val GraphFileName = "dependency-graph.md"
        }
    }

    /**
     * Creates mermaid graphs for all modules in the app and places each graph within the module's
     * folder. An app-wide graph is also created and added to the project's root directory.
     *
     *
     * [Derived from JakeWharton/SdkSearch](https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle)
     *
     *
     * Key differences are:
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
    @TaskAction
    fun createDependencyGraph() {
        // Create graph of all dependencies
        val graph = createGraph(project.rootProject)

        // For each module, draw its sub graph of dependencies and dependents
        graph.projects.forEach { drawDependencies(it, graph, false, project.rootDir) }

        // Draw the full graph of all modules
        drawDependencies(project.rootProject, graph, true, project.rootDir)

//        val prettyTag = tag.orNull?.let { "[$it]" } ?: ""
//
//        logger.lifecycle("$prettyTag message is: ${message.orNull}")
//        logger.lifecycle("$prettyTag tag is: ${tag.orNull}")
//        logger.lifecycle("$prettyTag outputFile is: ${outputFile.orNull}")
//
//        outputFile.get().asFile.writeText("$prettyTag ${message.get()}")
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    /**
     * Create a graph of all project modules, their types, dependencies and root projects.
     * @return An object of type GraphDetails containing all details
     */
    private fun createGraph(rootProject: Project): GraphDetails {
        val rootProjects = mutableListOf<Project>()
        var queue = mutableListOf(rootProject)

        // Traverse the list of all subfolders starting with root project and add them to
        // rootProjects
        while (queue.isNotEmpty()) {
            val project = queue.removeAt(0)
            if (project.name != GraphDetails.SystemTestName) {
                rootProjects.add(project)
            }
            queue.addAll(project.childProjects.values)
        }

        val projects = LinkedHashSet<Project>()
        val dependencies = LinkedHashMap<DependencyPair, List<String>>()
        val multiplatformProjects = mutableListOf<Project>()
        val androidProjects = mutableListOf<Project>()
        val javaProjects = mutableListOf<Project>()

        // Again traverse the list of all subfolders starting with the current project
        // * Add projects to project-type lists
        // * Add project dependencies to dependency hashmap with record for api/impl
        // * Add projects & their dependencies to projects list
        // * Remove any dependencies from rootProjects list
        queue = mutableListOf(rootProject)
        while (queue.isNotEmpty()) {
            val project = queue.removeAt(0)
            if (project.name == GraphDetails.SystemTestName) {
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
                    .forEach { dependency ->
                        projects.add(project)
                        projects.add(dependency)
                        if (
                            project.name != GraphDetails.SystemTestName &&
                            project.path != dependency.path
                        ) {
                            rootProjects.remove(dependency)
                        }

                        val graphKey = DependencyPair(project, dependency)
                        val traits = dependencies.computeIfAbsent(graphKey) { mutableListOf() }
                            .toMutableList()

                        if (config.name.lowercase(Locale.getDefault()).endsWith("implementation")) {
                            traits.add("impl")
                        } else {
                            traits.add("api")
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

        // TODO: 16/06/2023
//    projects = projects
//        .sortedBy { it.path }

        return GraphDetails(
            projects = projects,
            dependencies = dependencies,
            multiplatformProjects = multiplatformProjects,
            androidProjects = androidProjects,
            javaProjects = javaProjects,
            rootProjects = rootProjects,
        )
    }

    /**
     * Returns a list of all modules that are direct or indirect dependencies of the provided module
     * @param currentProjectAndDependencies the module(s) whose dependencies we need
     * @param dependencies hash map of dependencies generated by [createGraph]
     * @return List of module and all its direct & indirect dependencies
     */
    private fun gatherDependencies(
        currentProjectAndDependencies: MutableList<Project>,
        dependencies: LinkedHashMap<DependencyPair, List<String>>,
    ): MutableList<Project> {
        var addedNew = false
        dependencies
            .map { it.key }
            .forEach { (currProject, dependencyProject) ->
                if (
                    currentProjectAndDependencies.contains(currProject) &&
                    !currentProjectAndDependencies.contains(dependencyProject)
                ) {
                    currentProjectAndDependencies.add(dependencyProject)
                    addedNew = true
                }
            }
        return if (addedNew) {
            gatherDependencies(
                currentProjectAndDependencies = currentProjectAndDependencies,
                dependencies = dependencies,
            )
        } else {
            currentProjectAndDependencies
        }
    }

    /**
     * Returns a list of all modules that depend on the given module
     * @param currentProject the module whose dependencies we need
     * @param dependencies hash map of dependencies generated by [createGraph]
     * @return List of all modules that depend on the given module
     */
    private fun gatherDependents(
        currentProject: Project,
        dependencies: LinkedHashMap<DependencyPair, List<String>>,
    ): List<Project> {
        return dependencies
            .filter { (key, _) ->
                key.target == currentProject
            }
            .map { (key, _) -> key.origin }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    /**
     * Creates a graph of dependencies for the given project and writes it to a file in the project's
     * directory.
     */
    private fun drawDependencies(
        currentProject: Project,
        graphDetails: GraphDetails,
        isRootGraph: Boolean,
        rootDir: File,
    ) {
        val projects: LinkedHashSet<Project> = graphDetails.projects
        val dependencies: LinkedHashMap<DependencyPair, List<String>> =
            graphDetails.dependencies
        val multiplatformProjects = graphDetails.multiplatformProjects
        val androidProjects = graphDetails.androidProjects
        val javaProjects = graphDetails.javaProjects
        val rootProjects = graphDetails.rootProjects

        val currentProjectDependencies =
            gatherDependencies(mutableListOf(currentProject), dependencies)
        val dependents = gatherDependents(currentProject, dependencies)

        var fileText = """
    ```mermaid
    %%{ init: { 'theme': 'base' } }%%
    graph LR;\n
    %% Styling for module nodes by type
    classDef rootNode stroke-width:4px;
    classDef mppNode fill:#ffd2b3;
    classDef andNode fill:#baffc9;
    classDef javaNode fill:#ffb3ba;

    %% Modules

        """.trimIndent()
        // This ensures the graph is wrapped in a box with a background, so it's consistently visible
        // when rendered in dark mode.
        fileText += """
    subgraph
      direction LR

        """.trimIndent()

        val normalNodeStart = "(["
        val normalNodeEnd = "])"
        val rootNodeStart = "["
        val rootNodeEnd = "]"
        val javaNodeStart = "{{"
        val javaNodeEnd = "}}"

        var clickText = ""

        for (project in projects) {
            if (
                !isRootGraph &&
                !(currentProjectDependencies.contains(project) || dependents.contains(project))
            ) {
                continue
            }
            val isRoot = if (isRootGraph) {
                rootProjects.contains(project) || project == currentProject
            } else {
                project == currentProject
            }

            var nodeStart = if (isRoot) {
                rootNodeStart
            } else {
                normalNodeStart
            }
            var nodeEnd = if (isRoot) {
                rootNodeEnd
            } else {
                normalNodeEnd
            }

            val nodeClass = if (multiplatformProjects.contains(project)) {
                ":::mppNode"
            } else if (androidProjects.contains(project)) {
                ":::andNode"
            } else if (javaProjects.contains(project)) {
                if (!isRoot) {
                    nodeStart = javaNodeStart
                    nodeEnd = javaNodeEnd
                }
                ":::javaNode"
            } else {
                ""
            }

            fileText += "  ${project.path}${nodeStart}${project.path}${nodeEnd}$nodeClass;\n"

//        val relativePath = rootDir.relativePath(project.projectDir)
            val relativePath = project.projectDir.relativeTo(rootDir)
            clickText += "click ${project.path} ${GraphDetails.RepoPath}/${relativePath}\n"
        }

        fileText += """
    end

    %% Dependencies

        """.trimIndent()

        dependencies
            .filter { (key, _) ->
                val (origin, target) = key
                (isRootGraph || currentProjectDependencies.contains(origin)) &&
                    origin.path != target.path
            }
            .forEach { (key, traits) ->
                val (origin, target) = key
                val isApi = traits.isNotEmpty() && traits[0] == "api"
                val isDirectDependency = origin == currentProject

                val arrow = when {
                    isApi && isDirectDependency -> "==API===>"
                    isApi -> "--API--->"
                    isDirectDependency -> "===>"
                    else -> "--->"
                }
                fileText += "${origin.path}${arrow}${target.path}\n"
            }

        fileText += """
        %% Dependents

        """.trimIndent()

        dependencies
            .filter { (key, _) ->
                val (origin, target) = key
                dependents.contains(origin) &&
                    target == currentProject &&
                    origin.path != target.path
            }
            .forEach { (key, traits) ->
                val (origin, target) = key
                // bold dashed arrows aren't supported
                val isApi = traits.isNotEmpty() && traits[0] == "api"
                val arrow = if (isApi) {
                    "-.API.->"
                } else {
                    "-.->"
                }
                fileText += "${origin.path}${arrow}${target.path}\n"
            }

        fileText += """

    %% Click interactions
    $clickText
    ```
        """.trimIndent()

        val graphFile = File(currentProject.projectDir, GraphDetails.GraphFileName)
        graphFile.parentFile.mkdirs()
        graphFile.delete()
        graphFile.writeText(fileText)

        println("Project module dependency graph created at ${graphFile.absolutePath}")
    }
}