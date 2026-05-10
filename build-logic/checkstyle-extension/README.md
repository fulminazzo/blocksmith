Extension module for [checkstyle](https://checkstyle.sourceforge.io/) configuration.

The configuration will provide some sets of rules:

- each class declaration should have **fields** first, then **methods**, finally **nested classes and interfaces**;
- the **fields** should be declared in the following order:
    - **static** fields before **instance**;
    - **final** fields before **non-final**;
    - **visibility ordering**: **public**, **protected**, **package** and **private**.
- then **constructors** should be declared in the following order:
    - **visibility ordering**: (check **fields**);
    - **overloads ordering**: constructors with fewer parameters first,
      constructors with the same amount should prioritize **primitive parameters** over general types.
- then **methods** should be declared in the following order:
    - **instance** methods before **static**;
    - (for **instance** methods) **ownership ordering**: **abstract**, **concrete**, **override**;
    - **visibility-ordering**: (check **fields**);
    - **overloads ordering**: method overloads should be grouped (meaning there should not be non-overloaded
      methods in between). Among that grouping, methods with fewer parameters should come first,
      methods with the same amount should prioritize **primitive parameters** over general types;
    - **method naming ordering**: based on the method name, the following rules should be applied:
        - any other method not described in the following rules;
        - `addAll`, `appendAll`, `putAll` and `removeAll`, `deleteAll` prefixed methods;
        - `register` and `unregister` prefixed methods;
        - `add`, `append`, `put` and `remove`, `delete` prefixed methods;
        - `is`, `get` and `set` prefixed methods;
        - `equals`, `toString` and `hashCode`.
- finally, **classes and interfaces** should be declared in the following order:
    - **type ordering**: **interface**, **enum**, **record** and **class**;
    - **static** types before **instance**;
    - **visibility ordering**: (check **fields**);
    - each of the rules in this document will be applied recursively to classes and interfaces as well.

Also, if [Lombok](https://projectlombok.org/) is in use, the following annotation order must be respected:

- `@Data`
- `@Value`
- `@Getter`
- `@Setter`
- `@EqualsAndHashCode`
- `@ToString`
- `@NoArgsConstructor`
- `@RequiredArgsConstructor`
- `@AllArgsConstructor`
- `@Builder`
