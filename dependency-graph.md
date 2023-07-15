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
  andNode([Android]):::andNode;
  javaNode{{Java}}:::javaNode;
  mppNode([Multi-platform]):::mppNode;
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
click :example:data https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/data/dependency-graph.md
click :example:domain https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/domain/dependency-graph.md
click :example:feature https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/feature/dependency-graph.md
click :example:models https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/models/dependency-graph.md
click :example:ui https://github.com/adityabhaskar/Project-Dependency-Graph/blob/main/example/ui/dependency-graph.md
```