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
    :example:data{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/data/dependencyGraph.md' style='text-decoration:auto'>:example:data</a>}}:::javaNode;
    :example:domain{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/domain/dependencyGraph.md' style='text-decoration:auto'>:example:domain</a>}}:::javaNode;
    :example:models[<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/models/dependencyGraph.md' style='text-decoration:auto'>:example:models</a>]:::javaNode;
    :example:shared-ui{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/shared-ui/dependencyGraph.md' style='text-decoration:auto'>:example:shared-ui</a>}}:::javaNode;
    :example:ui{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/ui/dependencyGraph.md' style='text-decoration:auto'>:example:ui</a>}}:::javaNode;
    subgraph theNewThing
      direction LR;
      :example:theNewThing:data{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/data/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing:data</a>}}:::javaNode;
      :example:theNewThing:ui{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/ui/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing:ui</a>}}:::javaNode;
    end

    subgraph thePremiumThing
      direction LR;
      :example:thePremiumThing:data{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/thePremiumThing/data/dependencyGraph.md' style='text-decoration:auto'>:example:thePremiumThing:data</a>}}:::javaNode;
      :example:thePremiumThing:ui{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/thePremiumThing/ui/dependencyGraph.md' style='text-decoration:auto'>:example:thePremiumThing:ui</a>}}:::javaNode;
    end
  end
end

%% Dependencies

%% Dependents
:example:data-.->:example:models
:example:ui-.->:example:models
:example:shared-ui-.->:example:models
:example:domain-.API.->:example:models
:example:theNewThing:data-.->:example:models
:example:theNewThing:ui-.->:example:models
:example:thePremiumThing:data-.->:example:models
:example:thePremiumThing:ui-.->:example:models
```