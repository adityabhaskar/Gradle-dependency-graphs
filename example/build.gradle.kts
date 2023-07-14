plugins {
    kotlin("jvm")
    id("net.c306.dependencydiagram.plugin")
}

//extensions.getByType(net.c306.dependencydiagram.plugin.DependencyGraphExtension::class.java)

projectDependencyGraphConfig {

//    message.set("Just trying this gradle plugin...")
}

dependencies {
    implementation(project(path = ":ui"))
    implementation(project(path = ":domain"))
}