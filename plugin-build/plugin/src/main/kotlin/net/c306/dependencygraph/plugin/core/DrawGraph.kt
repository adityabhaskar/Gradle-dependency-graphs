package net.c306.dependencygraph.plugin.core

import net.c306.dependencygraph.plugin.DependencyPair
import net.c306.dependencygraph.plugin.ModuleProject
import net.c306.dependencygraph.plugin.ParsedGraph
import net.c306.dependencygraph.plugin.ShowLegend
import java.io.File

@Suppress("LongMethod", "CyclomaticComplexMethod")
/**
 * Creates a graph of dependencies for the given project and writes it to a file in the project's
 * directory.
 */
internal fun drawDependencyGraph(
    currentProject: ModuleProject,
    parsedGraph: ParsedGraph,
    isRootGraph: Boolean,
    rootDir: File,
    moduleBaseUrl: String?,
    showLegend: ShowLegend,
    graphDirection: String,
    fileName: String,
) {
    val projects: LinkedHashSet<ModuleProject> = parsedGraph.projects
    val dependencies: LinkedHashMap<DependencyPair, List<String>> =
        parsedGraph.dependencies
    val multiplatformProjects = parsedGraph.multiplatformProjects
    val androidProjects = parsedGraph.androidProjects
    val javaProjects = parsedGraph.javaProjects
    val rootProjects = parsedGraph.rootProjects

    val currentProjectDependencies =
        gatherDependencies(mutableListOf(currentProject), dependencies)
    val dependents = currentProject.gatherDependents(dependencies)

    val legendText = when (showLegend) {
        ShowLegend.Always -> LegendText
        ShowLegend.OnlyInRootGraph -> if (isRootGraph) LegendText else ""
        ShowLegend.Never -> ""
    }

    var fileText = """
    ```mermaid
    %%{ init: { 'theme': 'base' } }%%
    graph LR;

    %% Styling for module nodes by type
    classDef rootNode stroke-width:4px;
    classDef mppNode fill:#ffd2b3;
    classDef andNode fill:#baffc9;
    classDef javaNode fill:#ffb3ba;
    $legendText
    %% Modules

        """.trimIndent()
    // This ensures the graph is wrapped in a box with a background, so it's consistently visible
    // when rendered in dark mode.
    fileText += "subgraph  \n  direction $graphDirection;\n"

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

        val relativePath = project.projectDir.relativeTo(rootDir)
        moduleBaseUrl?.let {
            clickText += "click ${project.path} ${it}/${relativePath}/${fileName}\n"
        }
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

    fileText += if (moduleBaseUrl == null) {
        "```"
    } else {
        """

%% Click interactions
$clickText```
""".trimIndent()
    }

    val graphFile = File(currentProject.projectDir, fileName)
    graphFile.parentFile.mkdirs()
    graphFile.delete()
    graphFile.writeText(fileText)

    println("Project module dependency graph created at ${graphFile.absolutePath}")
}

/**
 * Returns a list of all modules that are direct or indirect dependencies of the provided module
 * @param currentProjectAndDependencies the module(s) whose dependencies we need
 * @param dependencies hash map of dependencies generated by [parseDependencyGraph]
 * @return List of module and all its direct & indirect dependencies
 */
private fun gatherDependencies(
    currentProjectAndDependencies: MutableList<ModuleProject>,
    dependencies: LinkedHashMap<DependencyPair, List<String>>,
): MutableList<ModuleProject> {
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
 * @receiver the module whose dependencies we need
 * @param dependencies hash map of dependencies generated by [parseDependencyGraph]
 * @return List of all modules that depend on the given module
 */
private fun ModuleProject.gatherDependents(
    dependencies: LinkedHashMap<DependencyPair, List<String>>,
): List<ModuleProject> {
    return dependencies
        .filter { (key, _) ->
            key.target == this
        }
        .map { (key, _) -> key.origin }
}

private const val LegendText = """
    %% Graph types
    subgraph Legend
      direction TB;
      rootNode[Root/current module]:::rootNode;
      javaNode{{Java/Kotlin}}:::javaNode;
      andNode([Android]):::andNode;
      mppNode([Multi-platform]):::mppNode;
      subgraph Direct dependency
        direction LR;
        :a===>:b
      end
      subgraph Indirect dependency
        direction LR;
        :c--->:d
      end
      subgraph API dependency
        direction LR;
        :e--API--->:f
      end
    end
    """