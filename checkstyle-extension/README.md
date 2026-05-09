Extension module for [checkstyle](https://checkstyle.sourceforge.io/) configuration.

The configuration will provide some sets of rules:
- each class declaration should have **fields** first, then **methods**, finally **nested classes and interfaces**;
- the **fields** should be declared in the following order:
  - **static** fields before **instance**;
  - **visibility ordering**: **public**, **protected**, **package** and **private**;
  - **final** fields before **non-final**.
- then **constructors** should be declared in the following order:
  - **visibility ordering**: (check **fields**);
  - **overloads ordering**: constructors with fewer parameters first,
    constructors with the same amount should prioritize **primitives** over general objects.
- then **methods** should be declared in the following order:
  - **instance** methods before **static**;
  - (for **instance** methods) **ownership ordering**: **abstract**, **concrete**, **override**;
  - **visibility-ordering**: (check **fields**);
  - **overloads ordering**: (check **constructors**);
  - **method naming ordering**:
    - `equals`, `toString` and `hashCode` should come last (in the order they were presented);
    - **before** them, any getter and setter should be coupled (`setValue` after `getValue`, unless overloads);
    - **before** them, any other method.
- finally, **classes and interfaces** should be declared in the following order:
  - **type ordering**: **interface**, **enum**, **record** and **class**;
  - **static** types before **instance**;
  - **visibility ordering**: (check **fields**);
  - each of the rules in this document will be applied recursively to classes and interfaces as well.
