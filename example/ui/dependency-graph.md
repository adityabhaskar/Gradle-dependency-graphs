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
  :example:models{{:example:models}}:::javaNode;
  :example:feature{{:example:feature}}:::javaNode;
  :example:ui[:example:ui]:::javaNode;
end

%% Dependencies
:example:ui===>:example:models

%% Dependents
:example:feature-.->:example:ui

%% Click interactions
click :example:models https://github.com/oorjalabs/todotxt-for-android/blob/main/example/models
click :example:feature https://github.com/oorjalabs/todotxt-for-android/blob/main/example/feature
click :example:ui https://github.com/oorjalabs/todotxt-for-android/blob/main/example/ui
```