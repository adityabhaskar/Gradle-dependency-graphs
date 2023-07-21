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
  :example:data{{<a href='/example/data/dependencyGraph.md' style='color:#333,text-decoration:auto'>:example:data</a>}}:::javaNode;
  :example:domain{{<a href='/example/domain/dependencyGraph.md' style='color:#333,text-decoration:auto'>:example:domain</a>}}:::javaNode;
  :example:feature[<a href='/example/feature/dependencyGraph.md' style='color:#333,text-decoration:auto'>:example:feature</a>]:::javaNode;
  :example:models{{<a href='/example/models/dependencyGraph.md' style='color:#333,text-decoration:auto'>:example:models</a>}}:::javaNode;
  :example:ui{{<a href='/example/ui/dependencyGraph.md' style='color:#333,text-decoration:auto'>:example:ui</a>}}:::javaNode;
end

%% Dependencies
:example:data--->:example:models
:example:feature--->:example:ui
:example:feature--->:example:domain
:example:ui--->:example:models
:example:domain--API--->:example:models
:example:domain--->:example:data

%% Dependents
```