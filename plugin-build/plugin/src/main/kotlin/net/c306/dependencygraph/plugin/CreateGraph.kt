package net.c306.dependencygraph.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import java.io.File
import java.util.*

@Suppress("LongMethod", "CyclomaticComplexMethod")
/**
 * Create a graph of all project modules, their types, dependencies and root projects.
 * @return An object of type GraphDetails containing all details
 */
internal fun parseDependencyGraph(rootProject: Project): GraphDetails {
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

                    val graphKey =
                        DependencyPair(project.asModuleProject(), dependency.asModuleProject())
                    val traits = dependencies
                        .computeIfAbsent(graphKey) { mutableListOf() } as MutableList

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

    return GraphDetails(
        projects = LinkedHashSet(projects.map { it.asModuleProject() }.sortedBy { it.path }),
        dependencies = dependencies,
        multiplatformProjects = multiplatformProjects.map { it.asModuleProject() },
        androidProjects = androidProjects.map { it.asModuleProject() },
        javaProjects = javaProjects.map { it.asModuleProject() },
        rootProjects = rootProjects.map { it.asModuleProject() },
    )
}

/**
 * Returns a list of all modules that are direct or indirect dependencies of the provided module
 * @param currentProjectAndDependencies the module(s) whose dependencies we need
 * @param dependencies hash map of dependencies generated by [parseDependencyGraph]
 * @return List of module and all its direct & indirect dependencies
 */
internal fun gatherDependencies(
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
internal fun ModuleProject.gatherDependents(
    dependencies: LinkedHashMap<DependencyPair, List<String>>,
): List<ModuleProject> {
    return dependencies
        .filter { (key, _) ->
            key.target == this
        }
        .map { (key, _) -> key.origin }
}


@Suppress("LongMethod", "CyclomaticComplexMethod")
/**
 * Creates a graph of dependencies for the given project and writes it to a file in the project's
 * directory.
 */
internal fun drawDependencies(
    currentProject: ModuleProject,
    graphDetails: GraphDetails,
    isRootGraph: Boolean,
    rootDir: File,
    moduleBaseUrl: String?,
    showLegend: ShowLegend,
    graphDirection: String,
) {
    val projects: LinkedHashSet<ModuleProject> = graphDetails.projects
    val dependencies: LinkedHashMap<DependencyPair, List<String>> =
        graphDetails.dependencies
    val multiplatformProjects = graphDetails.multiplatformProjects
    val androidProjects = graphDetails.androidProjects
    val javaProjects = graphDetails.javaProjects
    val rootProjects = graphDetails.rootProjects

    val currentProjectDependencies =
        gatherDependencies(mutableListOf(currentProject), dependencies)
    val dependents = currentProject.gatherDependents(dependencies)

    val legendText = when(showLegend) {
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
            clickText += "click ${project.path} ${it}/${relativePath}/${GraphDetails.GraphFileName}\n"
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

    val graphFile = File(currentProject.projectDir, GraphDetails.GraphFileName)
    graphFile.parentFile.mkdirs()
    graphFile.delete()
    graphFile.writeText(fileText)

    println("Project module dependency graph created at ${graphFile.absolutePath}")
}


private const val LegendText = """
    %% Graph types
    subgraph Legend
      direction TB;
      rootNode[Root/current module]:::rootNode;
      andNode([Android]):::andNode;
      javaNode{{Java}}:::javaNode;
      mppNode([Multi-platform]):::mppNode;
    end
    """