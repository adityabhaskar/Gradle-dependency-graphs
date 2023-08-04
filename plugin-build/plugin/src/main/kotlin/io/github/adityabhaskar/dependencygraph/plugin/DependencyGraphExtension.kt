package io.github.adityabhaskar.dependencygraph.plugin

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass", "UndocumentedPublicClass")
abstract class DependencyGraphExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    /**
     * Optional list of modules to be ignored when generating the graph. This may be used, for instance to
     * remove system test modules to see only the production graph.
     *
     * Provide full path strings, e.g. `:live-feature:ui` instead of `:test-ui` of the modules you
     * want to ignore.
     */
    val ignoreModules: ListProperty<String> = objects.listProperty(String::class.java)

    /**
     * Optional Github URL for your repository. E.g. `https://github.com/adityabhaskar/Gradle-dependency-graphs`
     *
     * The URL is used for adding links to modules to allow navigation to a module's subgraph just
     * by clicking on it. If no URL is provided, then links aren't added to the graph.
     *
     * **Note**: Github doesn't support click navigation from mermaid graphs at the
     * moment.
     */
    val repoRootUrl: Property<String> = objects.property(String::class.java)

    /**
     * Optional name of your main branch, e.g. `master`. Default is `main`.
     *
     * This is combined with the [repoRootUrl] to create clickable URLs. The URLs are used for
     * adding links to graph to allow navigation to a module's subgraph by clicking on a module.
     * If no [repoRootUrl] is provided, then links aren't added to the graph.
     *
     * **Note**: Github doesn't support click navigation from mermaid graphs at the
     * moment.
     */
    val mainBranchName: Property<String> = objects.property(String::class.java)

    /**
     * Optional name for the file where graph is saved. Default is `dependencyGraph.md`.
     * If the provided filename doesn't end in `.md`, then the extension will be appended.
     */
    val graphFileName: Property<String> = objects.property(String::class.java)

    /**
     * Whether module name text should link to graphs for that module.
     *
     * Github doesn't support click navigation from mermaid graphs at the moment. Linking the text
     * instead provides a work around for allowing navigating between subgraphs.
     */
    val shouldLinkModuleText: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Optional setting to define whether to show a legend identifying different types of modules.
     *
     * * [ShowLegend.Always] will add a legend to all graphs
     * * [ShowLegend.OnlyInRootGraph] will only add a legend to the root graph
     * * [ShowLegend.Never] will not add a legend to any graph
     *
     * Default value is [ShowLegend.OnlyInRootGraph]
     */
    val showLegend: Property<ShowLegend> = objects.property(ShowLegend::class.java)

    /**
     * Optionally group submodules under same folder together in graph. Default is `false`.
     */
    val shouldGroupModules: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Optional graph direction. Defaults to top down ([Direction.LeftToRight]).
     */
    val graphDirection: Property<Direction> = objects.property(Direction::class.java)
}