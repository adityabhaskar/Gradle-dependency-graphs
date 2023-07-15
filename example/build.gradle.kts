import io.github.adityabhaskar.dependencygraph.plugin.Direction
import io.github.adityabhaskar.dependencygraph.plugin.ShowLegend

plugins {
    id("io.github.adityabhaskar.dependencygraph")
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