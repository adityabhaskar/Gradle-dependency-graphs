package io.github.adityabhaskar.dependencygraph.plugin.core

import io.github.adityabhaskar.dependencygraph.plugin.DependencyPair
import io.github.adityabhaskar.dependencygraph.plugin.ModuleProject
import io.github.adityabhaskar.dependencygraph.plugin.ParsedGraph
import io.github.adityabhaskar.dependencygraph.plugin.ShowLegend
import java.io.File

internal data class DrawConfig(
    val rootDir: File,
    val moduleBaseUrl: String?,
    val showLegend: ShowLegend,
    val graphDirection: String,
    val fileName: String,
    val shouldLinkModuleText: Boolean,
    val shouldGroupModules: Boolean,
)

@Suppress("LongMethod", "CyclomaticComplexMethod", "CognitiveComplexMethod", "ktlint:indent")
/**
 * Creates a graph of dependencies for the given project and writes it to a file in the project's
 * directory.
 */
internal fun drawDependencyGraph(
    currentProject: ModuleProject,
    parsedGraph: ParsedGraph,
    isRootGraph: Boolean,
    config: DrawConfig,
) {
    val projects: LinkedHashSet<ModuleProject> = parsedGraph.projects
    val dependencies: LinkedHashMap<DependencyPair, List<String>> =
        parsedGraph.dependencies

    val currentProjectDependencies =
        gatherDependencies(mutableListOf(currentProject), dependencies)
    val dependents = currentProject.gatherDependents(dependencies)

    val legendText = when (config.showLegend) {
        ShowLegend.Always -> LegendText
        ShowLegend.OnlyInRootGraph -> if (isRootGraph) LegendText else ""
        ShowLegend.Never -> ""
    }

    var fileText = """
    ```mermaid
    %%{
      init: {
        "theme": "base",
        "themeVariables": {
          "nodeTextColor": "#333333",
          "clusterBorder": "#a0a0a0",
          "nodeBorder": "#767676"
        }
      }
    }%%

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
    fileText += "subgraph  \n  direction ${config.graphDirection};\n"

    val relevantProjects = if (isRootGraph) {
        projects.toList()
    } else {
        projects.filter {
            currentProjectDependencies.contains(it) ||
                dependents.contains(it) ||
                it == currentProject
        }
    }

    val modulesText = if (config.shouldGroupModules) {
        var rootMap = mutableMapOf<String, ProjectOrSubMap>()

        for (project in relevantProjects) {
            rootMap = groupSubModules(
                currentPath = "",
                project = project,
                remainingPath = project.path,
                map = rootMap,
            )
        }

        printProjectsGrouped(
            projectMap = rootMap,
            isRootGraph = isRootGraph,
            config = config,
            currentProject = currentProject,
            parsedGraph = parsedGraph,
        )
    } else {
        printProjects(
            projects = relevantProjects,
            isRootGraph = isRootGraph,
            config = config,
            currentProject = currentProject,
            parsedGraph = parsedGraph,
        )
    }

    fileText += modulesText + """
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
                isApi && isDirectDependency -> ConnectorType.DirectApi
                isApi -> ConnectorType.IndirectApi
                isDirectDependency -> ConnectorType.Direct
                else -> ConnectorType.Indirect
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

    fileText += "```"

    val graphFile = File(currentProject.projectDir, config.fileName)
    graphFile.parentFile.mkdirs()
    graphFile.delete()
    graphFile.writeText(fileText)

    println("Project module dependency graph created at ${graphFile.absolutePath}")
}

@Suppress("LongParameterList", "CognitiveComplexMethod")
private fun printProjects(
    projects: List<ModuleProject>,
    currentProject: ModuleProject,
    parsedGraph: ParsedGraph,
    isRootGraph: Boolean,
    config: DrawConfig,
    level: Int = 0,
): String {
    val multiplatformProjects = parsedGraph.multiplatformProjects
    val androidProjects = parsedGraph.androidProjects
    val javaProjects = parsedGraph.javaProjects
    val rootProjects = parsedGraph.rootProjects

    return projects.joinToString(separator = "\n", postfix = "\n") { project ->
        val isRoot = if (isRootGraph) {
            rootProjects.contains(project) || project == currentProject
        } else {
            project == currentProject
        }

        var nodeStart = if (isRoot) {
            NodeEnds.RootStart
        } else {
            NodeEnds.NormalStart
        }
        var nodeEnd = if (isRoot) {
            NodeEnds.RootEnd
        } else {
            NodeEnds.NormalEnd
        }

        val nodeClass = if (multiplatformProjects.contains(project)) {
            NodeClass.Mpp
        } else if (androidProjects.contains(project)) {
            NodeClass.Android
        } else if (javaProjects.contains(project)) {
            if (!isRoot) {
                nodeStart = NodeEnds.JavaStart
                nodeEnd = NodeEnds.JavaEnd
            }
            NodeClass.Java
        } else {
            ""
        }

        val relativePath = project.projectDir.relativeTo(config.rootDir)
        val nodeText = if (config.shouldLinkModuleText && config.moduleBaseUrl != null) {
            val link = "${config.moduleBaseUrl}/$relativePath/${config.fileName}"
            "<a href='$link' style='text-decoration:auto'>${project.path}</a>"
        } else {
            project.path
        }

        val indent = "  ".repeat(level) + "  "
        "$indent${project.path}${nodeStart}${nodeText}${nodeEnd}$nodeClass;"
    }
}

@Suppress("LongParameterList")
private fun printProjectsGrouped(
    projectMap: MutableMap<String, ProjectOrSubMap>,
    currentProject: ModuleProject,
    parsedGraph: ParsedGraph,
    isRootGraph: Boolean,
    config: DrawConfig,
    groupName: String = currentProject.path,
    level: Int = 0,
): String {
    val currentLevelProjects = projectMap.mapNotNull { (_, value) ->
        value.project
    }

    val indent = "  ".repeat(level)
    var subModuleString = if (currentLevelProjects.isEmpty()) {
        ""
    } else {
        "${indent}subgraph $groupName\n$indent  direction LR;\n"
    }

    subModuleString += printProjects(
        currentLevelProjects,
        currentProject,
        parsedGraph,
        isRootGraph,
        config,
        level,
    )

    subModuleString += projectMap
        .mapNotNull { (name, value) ->
            value.subMap?.let { Pair(name, it) }
        }
        .joinToString("\n") {
            printProjectsGrouped(
                projectMap = it.second,
                currentProject = currentProject,
                parsedGraph = parsedGraph,
                isRootGraph = isRootGraph,
                config = config,
                groupName = it.first,
                level = level + 1,
            )
        }

    val end = if (currentLevelProjects.isEmpty()) {
        ""
    } else {
        "${indent}end\n"
    }
    return subModuleString + end
}

private data class ProjectOrSubMap(
    val project: ModuleProject? = null,
    val subMap: MutableMap<String, ProjectOrSubMap>? = null,
) {
    override fun toString() = when {
        project != null && subMap != null -> "ProjectAndSubMap(project=$project, submap=$subMap)"
        project != null -> project.path
        subMap != null -> "$subMap"
        else -> ""
    }
}

private fun groupSubModules(
    currentPath: String,
    project: ModuleProject,
    remainingPath: String,
    map: MutableMap<String, ProjectOrSubMap>,
): MutableMap<String, ProjectOrSubMap> {
    val segments = remainingPath
        .removePrefix(":")
        .split(":")
    val nextPath = segments.take(1)
    val remaining = segments.drop(1)
    val nowRemainingPath = remaining.joinToString(
        separator = ":",
        prefix = ":",
    )

    return if (nextPath.isEmpty()) {
        map
    } else if (remaining.isEmpty()) {
        map.apply {
            putIfAbsent(nextPath[0].ifBlank { ":" }, ProjectOrSubMap(project = project))
        }
    } else {
        map.apply {
            val key = if (currentPath.isBlank()) {
                nextPath[0]
            } else {
                "$currentPath:${nextPath[0]}"
            }
            val subMap = groupSubModules(
                currentPath = key,
                project = project,
                remainingPath = nowRemainingPath,
                map = get(key)?.subMap ?: mutableMapOf(),
            )
            put(key, ProjectOrSubMap(subMap = subMap))
        }
    }
}

private fun mapProjectListToGroups() {
    // TODO: 04/08/2023 When encountering a sub project, add it to a list but only record parent
    //  folder name Then when drawing dependencies, check the list and draw it to parent folder
    //  when sub project falls under it
}


/**
 * Returns a list of all modules that are direct or indirect dependencies of the provided module.
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
 * Returns a list of all modules that depend on the given module.
 * @receiver the module whose dependencies we need
 * @param dependencies hash map of dependencies generated by [parseDependencyGraph]
 * @return List of all modules that depend on the given module
 */
private fun ModuleProject.gatherDependents(
    dependencies: LinkedHashMap<DependencyPair, List<String>>,
) = dependencies
    .filter { (key, _) -> key.target == this }
    .map { (key, _) -> key.origin }

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

private object NodeClass {
    const val Mpp = ":::mppNode"
    const val Android = ":::andNode"
    const val Java = ":::javaNode"
}

private object ConnectorType {
    const val DirectApi = "==API===>"
    const val IndirectApi = "--API--->"
    const val Direct = "===>"
    const val Indirect = "--->"
}

private object NodeEnds {
    const val NormalStart = "(["
    const val NormalEnd = "])"
    const val RootStart = "["
    const val RootEnd = "]"
    const val JavaStart = "{{"
    const val JavaEnd = "}}"
}