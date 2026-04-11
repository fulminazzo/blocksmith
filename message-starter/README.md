The **message-starter** module eases creation of messages files with support for translation.
Uses [Text Adventure](https://docs.papermc.io/adventure/) and [MiniMessage](https://docs.papermc.io/adventure/minimessage/)
as **message components providers**, however the messages supports all kinds of formats:

```json
{
  "simple": "&aHello, §cworld!",
  "hex": "&#55FF55Hello, §x§F§F5§5§5§5world!",
  "minimessage": "<green>Hello, <#FF5555>world!"
}
```

To use the module you will need to create a new [Messenger](./src/main/java/it/fulminazzo/blocksmith/message/Messenger.java)
and load in a [MessageProvider](#messageprovider):

```java
Logger logger; // slf4j logger
Messenger messenger = new Messenger(logger);
MessageProvider provider; // provider
messenger.setMessageProvider(provider);
```

Usage of the new messenger is trivial:

- send a simple **chat** message to **one target**:

  ```java
  messenger.sendMessage(
          target, // the receiver of the message
          messageCode, // the code associated with the message
          /*
           * Arguments to pass in the message.
           * In this case, we are creating a new placeholder replacement:
           * %name% -> target#getName()
           */
          Placeholder.of("name", target.getName())
  );
  ```

- send an **actionbar** message to **one target**:

  ```java
  messenger.sendActionBar(
          target, // the receiver of the message
          messageCode // the code associated with the message
  );
  ```

- send a **title** message to **one target**:

  ```java
  messenger.sendTitle(
          target, // the receiver of the message
          titleCode, // the code associated with the message to display as title
          subtitleCode, // the code associated with the message to display as subtitle
          times // timings of the title (fade-in, duration and fade-out) using Text Adventure
  );
  ```

- broadcast a simple **chat** message:

  ```java
  messenger.broadcastMessage(
          messageCode, // the code associated with the message
          /*
           * Arguments to pass in the message.
           * In this case, we are creating a new placeholder replacement:
           * %name% -> "blocksmith"
           */
          Placeholder.of("name", "blocksmith")
  );
  ```

- broadcast an **actionbar** message:

  ```java
  messenger.broadcastActionBar(
          messageCode // the code associated with the message
  );
  ```

- broadcast a **title** message:

  ```java
  messenger.broadcastTitle(
          titleCode, // the code associated with the message to display as title
          subtitleCode, // the code associated with the message to display as subtitle
          times // timings of the title (fade-in, duration and fade-out) using Text Adventure
  );
  ```

**WARNING**: to use the Messenger send and broadcast methods, you **must** include a **platform module** in your project.
For **Bukkit**, you can import [message-starter-bukkit](/message-starter-bukkit).


### MessageProvider

Currently, **three types** of providers are available:

- **memory**: linked to a **Map** in the JVM:

  ```java
  /*
   * The messages map will be flattened and converted to a known format.
   * For example, assuming the map is:
   * 
   * {
   *   "name": "blocksmith",
   *   "error": {
   *     "startup": "Could not start up framework..."
   *   },
   *   "success": {
   *     "startup": ["The framework has been successfully enabled!", "Welcome to blocksmith!"],
   *     "welcome": "Welcome back!"
   *   }
   * }
   * 
   * it will be converted to:
   * 
   * {
   *   "name": "blocksmith",
   *   "error.startup": "Could not start up framework...",
   *   "success.startup": "The framework has been successfully enabled!\nWelcome to blocksmith!",
   *   "success.welcome": "Welcome back!"
   * }
   */
  Map<String, String> messages;
  MessageProvider provider = MessageProvider.memory(messages);
  ```
  
- **resource**: will check for the existence of a resource on disk, in the working directory.
  If it was not found, it will **dump it** from the **JDK resources**.
  Then, it will load it in a **Map** and will follow the same rules of **memory** for the loading:

  ```java
  File workingDir; // working directory
  MessageProvider provider = MessageProvider.resource(workingDir, "messages.yml");
  ```
  
- **translation**: will check for the existence of a directory on disk, in the working directory.
  If it was not found, it will **dump all the resources** contained in the **folder** from the **JDK resources**.
  Then, it will load all the resources in a **Map** and will follow the same rules of **memory** for the loading:

  ```java
  File workingDir; // working directory
  ConfigurationFormat resourcesFormat; // the format of the resources (YAML, JSON...)
  Logger logger; // slf4j logger
  MessageProvider provider = MessageProvider.translation(
          workingDir,
          "messages", // the name of the resources folder
          format,
          logger
  )
  ```
  
  **WARNING**: requires the [message-starter-translation](./message-starter-translation) module to be installed.
