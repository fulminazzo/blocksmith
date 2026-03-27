The **scheduler-starter** module is a **facade** for **scheduling tasks** through various platforms.
It attempts to uniform the creation of **tasks** to **execute later**, **repeatedly** or **asynchronously** 
with the same code, regardless of the implementation.

Creation of a task is trivial thanks to the [Scheduler](./src/main/java/it/fulminazzo/blocksmith/scheduler/Scheduler.java):

```java
Object plugin; // the owner of the task, generally the plugin instance
Scheduler.schedule(plugin, t -> {
    // something that might use Task
})
        // Will run the task after 1 second
        .delay(1L, TimeUnit.SECONDS)
        // Will run the task every 50 milliseconds
        .interval(Duration.ofMillis(50)) // Duration supported as well!
        // In some platforms, all the scheduled tasks are synchronous,
        // meaning the main executor will be blocked to run the task.
        // By creating an async task, the scheduler will be signaled
        // to run it along side the main one (use with caution)
        .async()
        .run();
```

**WARNING**: to use the Scheduler functions, you **must** include a **platform module** in your project.
For **Bukkit**, you can import [scheduler-starter-bukkit](/scheduler-starter-bukkit).

Along with the task mentioned before, other types are available:

- **repeated task**: this special type of task will be run **periodically** until **a certain condition is met**.
It is possible to use both **Java predicates** or the **number of times** it should run:

```java
Object plugin; // the owner of the task, generally the plugin instance
Scheduler.schedule(plugin, t -> {
    // something that might use Task
})
        .interval(1L, TimeUnit.SECONDS)
        // the task will be run only the specified amount of times
        .repeated(3);
```

- **query executor task**: when using certain platforms ([Bukkit](https://dev.bukkit.org/) based),
it is advised to run **database queries** or **API requests asynchronously**,
but the results should be used in a **synchronous context**.
The **Scheduler** provides an **helper function** for just that:

```java
Object plugin; // the owner of the task, generally the plugin instance
Scheduler.runAsyncThen(plugin,
        CompletableFuture.supplyAsync(() -> 18), // query simulation 
        a -> System.out.println("Age of the user: " + a) 
);
```

## Folia

[Folia](https://github.com/PaperMC/Folia) is supported as well.
Because **Folia** has **various types of tasks** depending on the usage,
it is **mandatory** to specify a **special owner** based on the target of the task:

- **asynchronous**: will run on [AsyncScheduler](https://jd.papermc.io/folia/io/papermc/paper/threadedregions/scheduler/class-use/AsyncScheduler.html),
therefore it only requires the **plugin** (as done previously).
- **synchronous** and **edits a location** or a **region**: will run on [RegionScheduler](https://jd.papermc.io/folia/1.21.11/io/papermc/paper/threadedregions/scheduler/class-use/RegionScheduler.html),
  so a **[Location](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Location.html) must be specified** (contained in the modified region).

```java
Object plugin; // the owner of the task, generally the plugin instance
Location location; // the location of the region
Scheduler.schedule(Pair.of(plugin, location), t -> {
    // something that might use Task
}).run();
```

- **synchronous** and **interacts with a specific entity**: will run on [EntityScheduler](https://jd.papermc.io/folia/1.21.11/io/papermc/paper/threadedregions/scheduler/class-use/EntityScheduler.html),
  so an **[Entity](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Entity.html) must be specified**.

```java
Object plugin; // the owner of the task, generally the plugin instance
Entity entity; // the entity
Scheduler.schedule(Pair.of(plugin, entity), t -> {
    // something that might use Task
}).run();
```

- **synchronous** (and does not interact with either regions of the world or entities): 
will run on [GlobalRegionScheduler](https://jd.papermc.io/folia/io/papermc/paper/threadedregions/scheduler/class-use/GlobalRegionScheduler.html),
therefore it only requires the **plugin** (as done previously).

The reason why for **locations** and **entities** a **pair** with the **plugin** must be specified, is that in case of
**non-Folia platform**, the Scheduler will attempt to use other available task builders to create the task (for example
in basic **Bukkit** environments).
