plugins {
    id("net.c306.dependencygraph.plugin")
}

dependencyGraphConfig {
    message.set("Just trying this gradle plugin...")

    tag.set("Something else")

    repoRootUrl.set("https://github.com/adityabhaskar/Project-Dependency-Graph/")

    showLegend.set(net.c306.dependencygraph.plugin.ShowLegend.OnlyInRootGraph)
}