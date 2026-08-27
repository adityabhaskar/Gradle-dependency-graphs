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
subgraph :example:theNewThing
  direction LR;
  :([<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main//dependencyGraph.md' style='text-decoration:auto'>:</a>]);
  subgraph example
    direction LR;
    :example:theNewThing[<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/theNewThing/dependencyGraph.md' style='text-decoration:auto'>:example:theNewThing</a>];
  end
end
end

%% Dependencies

%% Dependents
:-.->:example:theNewThing
```