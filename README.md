# Gradle 🐘 dependency graph visualisation plugin
[![Publish Plugin to Portal](https://github.com/adityabhaskar/Project-Dependency-Graph/workflows/Publish%20Plugin%20to%20Portal/badge.svg?branch=1.0.0)](https://github.com/adityabhaskar/Project-Dependency-Graph/actions?query=workflow%3A%22Publish+Plugin+to+Portal%22)
[![Pre Merge Checks](https://github.com/adityabhaskar/Project-Dependency-Graph/workflows/Pre%20Merge%20Checks/badge.svg)](https://github.com/adityabhaskar/Project-Dependency-Graph/actions?query=workflow%3A%22Pre+Merge+Checks%22)
[![License](https://img.shields.io/github/license/cortinico/kotlin-android-template.svg)](LICENSE)
![Language](https://img.shields.io/github/languages/top/cortinico/kotlin-android-template?color=blue&logo=kotlin)

A simple **Gradle Plugin** that generates dependency graphs showing the relationship between modules in your project.

The plugin generates a graph visualising the dependencies across the whole project. It also generates sub-graphs for each module within the project. For projects with a large number of modules, I find the sub-graphs tend to be a lot more useful.

The graphs are generated in the [`mermaid.js`](https://mermaid.js.org/syntax/flowchart.html#direction-in-subgraphs) format so they are automatically displayed by Github.

This plugin is a derivation of [Jake Wharton](https://github.com/JakeWharton/)'s project dependency graph script [available here](https://github.com/JakeWharton/SdkSearch/blob/master/gradle/projectDependencyGraph.gradle).

## How to use

### Apply the plugin

**Apply the plugin** to your project's root `build.gradle.kts`
```kotlin
plugins {
    id("io.github.adityabhaskar.dependencygraph")
}
```

### Using the plugin

The plugin adds a new Gradle task - `dependencyGraph`. Running the task will generate the dependency graphs for all modules in the project.

```bash
./gradlew dependencyGraph
```

### Configuring the plugin

Optionally **configure the plugin** in the same `build.gradle.kts` if you want to change the defaults
```kotlin
dependencyGraphConfig {
    graphDirection.set(Direction.LeftToRight)

    showLegend.set(ShowLegend.OnlyInRootGraph)

    ignoreModules.set(listOf(":example:system-test", ":example:test-fixtures"))

    repoRootUrl.set("https://github.com/adityabhaskar/Project-Dependency-Graph/")

    mainBranchName.set("main")

    graphFileName.set("dependencyGraph.md")
}
```

#### Configuration options

All configuration options are optional with sensible defaults.

<details open>
<summary><strong>graphDirection</strong></summary>

> Type: `Direction`
>
> The direction in which the graph should be laid out. Defaults to left to right (`Direction.LeftToRight`).
>
> Options:
> * `Direction.LeftToRight`
> * `Direction.TopToBottom`
> * `Direction.BottomToTop`
> * `Direction.RightToLeft`
</details>

</details>

<details open>
<summary><strong>showLegend</strong></summary>

> Type: `ShowLegend`
>
> Whether to show a legend. When enabled, the graph with contain a legend identifying different types of modules — current/root, java/kotlin, Android and multiplatform — and different type of dependencies - direct, indirect & transitive. Default value is `ShowLegend.OnlyInRootGraph`.
> * `ShowLegend.Always`
> * `ShowLegend.OnlyInRootGraph`
> * `ShowLegend.Never`

</details>

<details open>
<summary><strong>graphFileName</strong></summary>

> Type: `String`
>
> Name for the file where graph is saved. Default is `dependencyGraph.md`.
>
> **Note**:
> * If the provided filename doesn't end in `.md`, then the extension will be appended.
> * Try not to use `-` or any special characters in the file name. This interferes with the mermaid graph format when adding links. If the file name contains anything other than `[a-zA-Z0-9]`, then links will not be added.

</details>

<details open>
<summary><strong>ignoreModules</strong></summary>

> Type: `List<String></String>`
>
> A list of modules to be ignored when generating the graph. This may be used, for instance to remove system test modules to see only the production graph.
>
> Provide full path strings of the modules you want to ignore, e.g. `:live-feature:ui` instead of `:test-ui`.

</details>

<details open>
<summary><strong>repoRootUrl</strong></summary>

> Type: `String`
>
> Github URL for your repository. E.g. `https://github.com/adityabhaskar/Project-Dependency-Graph`
>
> The URL is used for adding links to modules to allow navigation to a module's subgraph just by clicking on it. If no URL is provided, then links aren't added to the graph.
>
> **Note**: Github doesn't support click navigation from mermaid graphs at the
moment.

</details>

<details open>
<summary><strong>mainBranchName</strong></summary>

> Type: `String`
>
> Name of your main branch, e.g. `master`. Default is `main`.
>
> This is combined with the [repoRootUrl] to create clickable URLs. The URLs are used for adding links to graph to allow navigation to a module's subgraph by clicking on a module. If no [repoRootUrl] is provided, then links aren't added to the graph.
>
> **Note**: Github doesn't support click navigation from mermaid graphs at the
moment.
</details>

## Dependency graphs

### Project wide dependency graph

The plugin will generate one graph at the root of the project for all modules in the project (except any ignored).

Sample root project graph with a legend:

```mermaid
%%{ init: { 'theme': 'base' } }%%
graph LR;

%% Styling for module nodes by type
classDef rootNode stroke-width:4px;
classDef mppNode fill:#ffd2b3;
classDef andNode fill:#baffc9;
classDef javaNode fill:#ffb3ba;

%% Graph types
subgraph Legend
  direction TB;
  rootNode[Root/current module]:::rootNode;
  javaNode{{Java/Kotlin}}:::javaNode;
  andNode([Android]):::andNode;
  mppNode([Multi-platform]):::mppNode;
  subgraph Direct dependency
    direction LR;
    :a===>:b
  end
  subgraph Indirect dependency
    direction LR;
    :c--->:d
  end
  subgraph API dependency
    direction LR;
    :e--API--->:f
  end
end

%% Modules
subgraph
  direction LR;
  :example:data{{:example:data}}:::javaNode;
  :example:domain{{:example:domain}}:::javaNode;
  :example:feature[:example:feature]:::javaNode;
  :example:models{{:example:models}}:::javaNode;
  :example:ui{{:example:ui}}:::javaNode;
end

%% Dependencies
:example:data--->:example:models
:example:feature--->:example:ui
:example:feature--->:example:domain
:example:ui--->:example:models
:example:domain--API--->:example:models
:example:domain--->:example:data

%% Dependents

%% Click interactions
click :example:data https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/data/dependencyGraph.md
click :example:domain https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/domain/dependencyGraph.md
click :example:feature https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/feature/dependencyGraph.md
click :example:models https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/models/dependencyGraph.md
click :example:ui https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/ui/dependencyGraph.md
```

### Submodule dependency graph

Further, the plugin will generate a graph for every module's dependencies within that module's root folder. This graph will include:
1. Direct dependents of the module, and
2. Direct and indirect dependencies of the module

_Dependents are identified with a dashed line._

Sample sub graph for the module `:example:domain` without a legend:

```mermaid
%%{ init: { 'theme': 'base' } }%%
graph LR;

%% Styling for module nodes by type
classDef rootNode stroke-width:4px;
classDef mppNode fill:#ffd2b3;
classDef andNode fill:#baffc9;
classDef javaNode fill:#ffb3ba;

%% Modules
subgraph
  direction LR;
  :example:data{{:example:data}}:::javaNode;
  :example:domain[:example:domain]:::javaNode;
  :example:feature{{:example:feature}}:::javaNode;
  :example:models{{:example:models}}:::javaNode;
end

%% Dependencies
:example:data--->:example:models
:example:domain==API===>:example:models
:example:domain===>:example:data

%% Dependents
:example:feature-.->:example:domain

%% Click interactions
click :example:data https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/data/dependencyGraph.md
click :example:domain https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/domain/dependencyGraph.md
click :example:feature https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/feature/dependencyGraph.md
click :example:models https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/models/dependencyGraph.md
```

## CI integration

> This section is a work in progress

### Creates a PR with changed graphs

Github action that creates a new PR if dependency graphs are updated on any push to `main`

**TBD**


### Commit changed graphs automatically

Github action that automatically commits new dependency graphs, if they are updated on any push to `main`

**TBD**


<!-- ## Publishing

This template is ready to let you publish to [Gradle Portal](https://plugins.gradle.org/).

The [![Publish Plugin to Portal](https://github.com/adityabhaskar/Project-Dependency-Graph/workflows/Publish%20Plugin%20to%20Portal/badge.svg?branch=1.0.0)](https://github.com/adityabhaskar/Project-Dependency-Graph/actions?query=workflow%3A%22Publish+Plugin+to+Portal%22) Github Action will take care of the publishing whenever you **push a tag**.

Please note that you need to configure two secrets: `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET` with the credetials you can get from your profile on the Gradle Portal. -->

## Contributing 🤝

Feel free to open a issue or submit a pull request for any bugs/improvements.

## License 📄

This template is licensed under the MIT License - see the [License](License) file for details.
