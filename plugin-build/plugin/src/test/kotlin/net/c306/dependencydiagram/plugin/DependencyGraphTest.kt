package net.c306.dependencydiagram.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DependencyDiagramTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("net.c306.dependencydiagram.plugin")

        assert(project.tasks.getByName("projectDependencyDiagram") is DependencyDiagramTask)
    }

    @Test
    fun `extension projectDependencyDiagramConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("net.c306.dependencydiagram.plugin")

        assertNotNull(project.extensions.getByName("projectDependencyDiagramConfig"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("net.c306.dependencydiagram.plugin")
//        val aFile = File(project.projectDir, ".tmp")
        (project.extensions.getByName("projectDependencyDiagramConfig") as DependencyDiagramExtension).apply {
            tag.set("a-sample-tag")
            message.set("just-a-message")
//            outputFile.set(aFile)
        }

        val task = project.tasks.getByName("projectDependencyDiagram") as DependencyDiagramTask

        assertEquals("a-sample-tag", task.tag.get())
        assertEquals("just-a-message", task.message.get())
//        assertEquals(aFile, task.outputFile.get().asFile)
    }
}