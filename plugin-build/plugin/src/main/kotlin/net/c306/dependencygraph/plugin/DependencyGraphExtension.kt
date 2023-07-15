package net.c306.dependencygraph.plugin

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class DependencyGraphExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    // Example of a property that is mandatory. The task will
    // fail if this property is not set as is annotated with @Optional.
    val message: Property<String> = objects.property(String::class.java)

    // Example of a property that is optional.
    val tag: Property<String> = objects.property(String::class.java)

    val ignoreModules: ListProperty<String> = objects.listProperty(String::class.java)
}