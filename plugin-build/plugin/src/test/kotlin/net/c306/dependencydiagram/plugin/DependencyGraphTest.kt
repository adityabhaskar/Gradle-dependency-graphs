package net.c306.dependencydiagram.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DependencyGraphTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("net.c306.dependencydiagram.plugin")

        assert(project.tasks.getByName("projectDependencyGraph") is DependencyGraphTask)
    }

    @Test
    fun `extension projectDependencyGraphConfig is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("net.c306.dependencydiagram.plugin")

        assertNotNull(project.extensions.getByName("projectDependencyGraphConfig"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("net.c306.dependencydiagram.plugin")
//        val aFile = File(project.projectDir, ".tmp")
        (project.extensions.getByName("projectDependencyGraphConfig") as DependencyGraphExtension).apply {
            tag.set("a-sample-tag")
            message.set("just-a-message")
//            outputFile.set(aFile)
        }

        val task = project.tasks.getByName("projectDependencyGraph") as DependencyGraphTask

        assertEquals("a-sample-tag", task.tag.get())
        assertEquals("just-a-message", task.message.get())
//        assertEquals(aFile, task.outputFile.get().asFile)
    }
}