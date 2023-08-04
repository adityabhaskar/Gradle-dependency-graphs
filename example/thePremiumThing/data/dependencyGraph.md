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
    :example:models{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/models/dependencyGraph.md' style='text-decoration:auto'>:example:models</a>}}:::javaNode;
    subgraph thePremiumThing
      direction LR;
      :example:thePremiumThing:data[<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/thePremiumThing/data/dependencyGraph.md' style='text-decoration:auto'>:example:thePremiumThing:data</a>]:::javaNode;
      :example:thePremiumThing:domain{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/thePremiumThing/domain/dependencyGraph.md' style='text-decoration:auto'>:example:thePremiumThing:domain</a>}}:::javaNode;
      :example:thePremiumThing:models{{<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/thePremiumThing/models/dependencyGraph.md' style='text-decoration:auto'>:example:thePremiumThing:models</a>}}:::javaNode;
    end
  end
end

%% Dependencies
:example:thePremiumThing:data===>:example:models
:example:thePremiumThing:data===>:example:thePremiumThing:models

%% Dependents
:example:thePremiumThing:domain-.->:example:thePremiumThing:data
```