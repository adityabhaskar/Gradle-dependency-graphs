package io.github.adityabhaskar.dependencygraph.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DependencyGraphTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("io.github.adityabhaskar.dependencygraph.plugin")

        assert(project.tasks.getByName("dependencyGraph") is DependencyGraphTask)
    }

    @Test
    fun `extension dependencyGraphConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("io.github.adityabhaskar.dependencygraph.plugin")

        assertNotNull(project.extensions.getByName("dependencyGraphConfig"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("io.github.adityabhaskar.dependencygraph.plugin")

        (project.extensions.getByName("dependencyGraphConfig") as DependencyGraphExtension).apply {
            graphDirection.set(Direction.BottomToTop)
            graphFileName.set("A_GRAPH.MD")
            mainBranchName.set("master")
            repoRootUrl.set("https://c306.net/")
            showLegend.set(ShowLegend.Never)
        }

        val task = project.tasks.getByName("dependencyGraph") as DependencyGraphTask

        assertEquals(Direction.BottomToTop, task.graphDirection.get())
        assertEquals("A_GRAPH.MD", task.graphFileName.get())
        assertEquals("master", task.mainBranchName.get())
        assertEquals("https://c306.net/", task.repoRootUrl.get())
        assertEquals(ShowLegend.Never, task.showLegend.get())
    }
}