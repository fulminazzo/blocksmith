This module provides functionality to **serialize** and **deserialize** configuration data from disk.
The currently supported data formats are:

- [YAML](https://yaml.org/)
- [JSON](https://json.org/)
- [TOML](https://toml.io/)
- [XML](https://wikipedia.org/wiki/XML)
- [Java properties](https://wikipedia.org/wiki/.properties)

but more might come in the future in case of many requests!

<br>

The usage of the module is quite trivial:
the only relevant class
is [ConfigurationAdapter](./src/main/java/it/fulminazzo/blocksmith/config/ConfigurationAdapter.java)
that allows creation of a new adapter to **store** and **load** **Java beans** from **files**.

It is possible to change the used format by using the method
`setFormat([ConfigurationFormat](./src/main/java/it/fulminazzo/blocksmith/config/ConfigurationFormat.java))`.

```java
Logger logger; // slf4j logger
ConfigurationAdapter adapter = ConfigurationAdapter.newAdapter(logger, ConfigurationFormat.YAML);
adapter.store(new File("data.yaml"), "Hello, world!");
// change format language of the adapter
adapter.setFormat(ConfigurationFormat.JSON);
adapter.load(new File("data.json"), String.class);
```
