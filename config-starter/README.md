This module provides functionality to **serialize** and **deserialize** configuration data from disk.
The currently supported data formats are:

- [JSON](https://json.org/) (requires the module `it.fulminazzo.blocksmith:config-starter-json`);
- [Java properties](https://wikipedia.org/wiki/.properties) (requires the module
  `it.fulminazzo.blocksmith:config-starter-properties`);
- [TOML](https://toml.io/) (requires the module `it.fulminazzo.blocksmith:config-starter-toml`);
- [XML](https://wikipedia.org/wiki/XML) (requires the module `it.fulminazzo.blocksmith:config-starter-xml`);
- [YAML](https://yaml.org/) (requires the module `it.fulminazzo.blocksmith:config-starter-yaml`);

but more might come in the future in case of many requests!

All of these can be accessed through the `it.fulminazzo.blocksmith:config-starter-all` module.

<br>

The usage of the module is quite trivial:
the only relevant class
is [ConfigurationAdapter](./src/main/java/it/fulminazzo/blocksmith/config/ConfigurationAdapter.java)
that allows creation of a new adapter to **store** and **load** **Java beans** from **files**.

It is possible to change the used format by using the method
`setFormat(ConfigurationFormat)` ([ConfigurationFormat](./src/main/java/it/fulminazzo/blocksmith/config/ConfigurationFormat.java)).

```java
File workingDir; // working directory
Logger logger; // slf4j logger
ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(logger, ConfigurationFormat.YAML);
adapter.store(workingDir, "data", "Hello, world!"); // extension of the file automatically determined
// change format language of the adapter
adapter.setFormat(ConfigurationFormat.JSON);
adapter.load(workingDir, "data", String.class);
```

## Comment annotation

The [ConfigurationAdapter](./src/main/java/it/fulminazzo/blocksmith/config/ConfigurationAdapter.java)
supports any type of Java POJO.

If a field is annotated with [Comment](./src/main/java/it/fulminazzo/blocksmith/config/Comment.java),
and the current data format language supports it,
the field will be saved with the specified comment.

```java
class User {
    
    @Comment({
            "The username of the user.",
            "Should be unique!"
    })
    private String username;
    
    @Comment("The email that the user specified at the time of registration.")
    private String email;
    
}
```
