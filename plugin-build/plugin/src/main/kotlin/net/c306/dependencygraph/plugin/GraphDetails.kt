package net.c306.dependencygraph.plugin

import groovy.lang.Tuple2
import org.gradle.api.Project

data class GraphDetails(
    val projects: LinkedHashSet<Project>,
    val dependencies: LinkedHashMap<Tuple2<Project, Project>, List<String>>,
    val multiplatformProjects: ArrayList<Project>,
    val androidProjects: ArrayList<Project>,
    val javaProjects: ArrayList<Project>,
    val rootProjects: ArrayList<Project>,
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
