# 09_Features

## 1. Builder Pattern for FieldDefinitions
Replaced the static Factory pattern (`FieldDefinitions.standard(...)`) with a type-safe **Builder Pattern**.
*   **Why**: Improves readability, prevents "telescoping constructor" antipattern, and ensures strict generic type safety.
*   **Usage**:
    ```java
    StandardFieldDefinition<String> def = StandardFieldDefinition.<String>builder()
        .defaultValue("Default")
        .build();
    ```

## 2. SOLID / SRP Refactoring
Refactored the core `ObjectMerger` classes to adhere to Single Responsibility Principle.
*   **`ObjectMerger`**: Now a Facade.
*   **`PojoMerger`**: Handles POJO merging.
*   **`MapMerger`**: Handles Map merging.
*   **`ReflectionHelper`**: Encapsulates reflection logic.

## 3. Generic Type Safety
Eliminated raw type usage across the library.
*   `FieldDefinition` -> `FieldDefinition<T>`
*   `MergeStrategy` -> `MergeStrategy<T, C extends FieldDefinition<T>>`
*   Ensures compile-time safety for strategy configuration.

## 4. Map-Based CLI
The CLI has been refactored to operate widely on `Map<String, Object>` structures, decoupling it from specific POJO classes like `Person`.
