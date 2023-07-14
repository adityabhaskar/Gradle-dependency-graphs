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
  :example:data[:example:data]:::javaNode;
  :example:models{{:example:models}}:::javaNode;
  :example:domain{{:example:domain}}:::javaNode;
end

%% Dependencies
:example:data===>:example:models

%% Dependents
:example:domain-.->:example:data

%% Click interactions
click :example:data https://github.com/oorjalabs/todotxt-for-android/blob/main/example/data
click :example:models https://github.com/oorjalabs/todotxt-for-android/blob/main/example/models
click :example:domain https://github.com/oorjalabs/todotxt-for-android/blob/main/example/domain
```