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
subgraph :example
  direction LR;
  :([<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main//dependencyGraph.md' style='text-decoration:auto'>:</a>]);
  :example[<a href='https://github.com/adityabhaskar/Gradle-dependency-graphs/blob/main/example/dependencyGraph.md' style='text-decoration:auto'>:example</a>];
end
end

%% Dependencies

%% Dependents
:-.->:example
```