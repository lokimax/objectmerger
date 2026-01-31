| Category | Feature | Priority | Status | Estimate | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Core** | **Refactor to SPI** | ⭐⭐⭐⭐⭐ | ✅ Complete | 🟡 M (8 pts) | Use Java ServiceLoader for Strategy discovery. |
| | **Recursive Merging** | ⭐⭐⭐⭐⭐ | ✅ Complete | 🟠🟠 L (13 pts) | Deep merge for nested objects and Maps. |
| **Strategies** | **Standard Strategy** | ⭐⭐⭐⭐ | ✅ Complete | 🟢 S (5 pts) | Default merging behavior for non-annotated fields. |
| | **Priority Strategy** | ⭐⭐⭐⭐⭐ | ✅ Complete | 🟡 M (8 pts) | Merge based on `@Priority` annotation. |
| | **Map Strategy** | ⭐⭐⭐⭐⭐ | ✅ Complete | 🔴 XL (21 pts) | Support for `Map<String, Object>` merging. |
| | **List Strategy** | ⭐⭐⭐ | ✅ Complete | 🟡 M (8 pts) | Advanced List merging (Recursive, IdentifyBy). |
| | **List Join Types** | ⭐⭐⭐ | ✅ Complete | 🟡 M (8 pts) | Support for Union, Intersection, and Key Origin (Template) joins for Lists. |
| | **Date Strategy** | ⭐⭐⭐ | ✅ Complete | 🟢 S (5 pts) | Merge based on Date/Time (Latest/Earliest). |
| | **Dynamic Strategy (MVEL)** | ⭐⭐⭐⭐ | ✅ Complete | 🟠 L (13 pts) | Runtime adaptation using MVEL expressions. |
| | **Time-Based Strategy** | ⭐⭐⭐ | ⚪ Not Started | 🟡 M (8 pts) | Time window or validity period merging. |
| | **Conditional Merging (MVEL)** | ⭐⭐⭐⭐ | ⚪ Not Started | 🔴 XL (21 pts) | Different merge logic using MVEL discriminators. |
| **Type Support** | **POJO Helper** | ⭐⭐⭐⭐ | ✅ Complete | 🟢 S (3 pts) | Utilities for POJO reflection. |
| | **Map <-> POJO** | ⭐⭐⭐ | ✅ Complete | 🟡 M (8 pts) | Interoperability via ObjectMergerService (Gson). |
| **Quality** | **Unit Tests** | ⭐⭐⭐⭐⭐ | ✅ Complete | 🟢 S (4 pts) | High coverage for all strategies. |
| | **Checkstyle/Spotless** | ⭐⭐⭐ | ✅ Complete | 🟢 S (2 pts) | Code formatting enforcement. |
| **Release** | **Maven Central** | ⭐⭐⭐⭐ | ⚪ Not Started | 🟡 M (8 pts) | Publish library to Maven Central. |
| **Tools** | **CLI Map-Only** | ⭐⭐⭐⭐ | ✅ Complete | 🟡 M (8 pts) | CLI refactored for exclusive Map<String, Object> support. |
| | **Definition Generator** | ⭐⭐⭐ | ✅ Complete | 🟡 M (8 pts) | CLI/Plugin to scaffold merge definitions from classes/data. |
| **Architecture** | **Typed Strategies** | ⭐⭐⭐ | ⚪ Not Started | 🟡 M (8 pts) | Improve Generic type safety for Strategies. (Review) |
| | **Strategy Constants** | ⭐⭐⭐ | ✅ Complete | 🟢 S (3 pts) | Decentralize strategy names via `getName()` (Refactored). |
| | **Composition (Interfaces)** | ⭐⭐⭐ | ⚪ Not Started | 🟡 M (8 pts) | Replace `FieldDefinition` inheritance with Interfaces (`Defaultable`, etc.). |
| **Performance** | **Reflection Caching** | ⭐⭐⭐ | ⚪ Not Started | 🟠 L (13 pts) | Cache Field/Method lookups or use MethodHandles. (Review) |
| **Robustness** | **Error Handling** | ⭐⭐⭐ | ⚪ Not Started | 🟡 M (5 pts) | Custom Exception hierarchy for precise error reporting. (Review) |
