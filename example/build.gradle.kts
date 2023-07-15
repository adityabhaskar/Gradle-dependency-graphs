plugins {
    id("net.c306.dependencygraph.plugin")
}

dependencyGraphConfig {
    message.set("Just trying this gradle plugin...")

    tag.set("Something else")

    // Optional
    repoRootUrl.set("https://github.com/adityabhaskar/Project-Dependency-Graph/")

    // Optional
    graphFileName.set("dependencyGraph.md")

    // Optional
    graphDirection.set(net.c306.dependencygraph.plugin.Direction.LeftToRight)

    // Optional
    showLegend.set(net.c306.dependencygraph.plugin.ShowLegend.OnlyInRootGraph)

    // Optional
    ignoreModules.set(listOf(":example:system-test"))
}