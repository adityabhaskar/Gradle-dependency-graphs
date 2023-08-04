```mermaid
%%{ init: { 'theme': 'base' } }%%
graph LR;

%% Styling for module nodes by type
classDef rootNode stroke-width:4px;
classDef mppNode fill:#ffd2b3,color:#333333;
classDef andNode fill:#baffc9,color:#333333;
classDef javaNode fill:#ffb3ba,color:#333333;

%% Modules
subgraph  
  direction LR;

  subgraph example
    direction LR;
    :example:app{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/app/dependencyGraph.md' style='text-decoration:auto'>:example:app</a>}}:::javaNode;
    :example:models{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/models/dependencyGraph.md' style='text-decoration:auto'>:example:models</a>}}:::javaNode;
    :example:shared-ui{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/shared-ui/dependencyGraph.md' style='text-decoration:auto'>:example:shared-ui</a>}}:::javaNode;
    subgraph theNewThing
      direction LR;
      :example:theNewThing:data{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/data/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing:data</a>}}:::javaNode;
      :example:theNewThing:domain{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/domain/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing:domain</a>}}:::javaNode;
      :example:theNewThing:feature[<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/feature/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing:feature</a>]:::javaNode;
      :example:theNewThing:models{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/models/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing:models</a>}}:::javaNode;
      :example:theNewThing:ui{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/ui/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing:ui</a>}}:::javaNode;
    end
  end
end

%% Dependencies
:example:shared-ui--->:example:models
:example:theNewThing:data--->:example:models
:example:theNewThing:data--->:example:theNewThing:models
:example:theNewThing:feature===>:example:theNewThing:ui
:example:theNewThing:feature===>:example:theNewThing:domain
:example:theNewThing:ui--->:example:models
:example:theNewThing:ui--->:example:theNewThing:models
:example:theNewThing:ui--->:example:shared-ui
:example:theNewThing:domain--API--->:example:theNewThing:models
:example:theNewThing:domain--->:example:theNewThing:data

%% Dependents
:example:app-.->:example:theNewThing:feature
```