import net.c306.dependencygraph.plugin.Direction
import net.c306.dependencygraph.plugin.ShowLegend

plugins {
    id("net.c306.dependencygraph.plugin")
}

dependencyGraphConfig {
    // Optional
    repoRootUrl.set("https://github.com/adityabhaskar/Project-Dependency-Graph/")

    // Optional
    mainBranchName.set("main")

    // Optional
    graphFileName.set("dependencyGraph.md")

    // Optional
    graphDirection.set(Direction.LeftToRight)

    // Optional
    showLegend.set(ShowLegend.OnlyInRootGraph)

    // Optional
    ignoreModules.set(listOf(":example:system-test"))
}