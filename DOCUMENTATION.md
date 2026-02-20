# ObjectMerger Strategy Documentation

This document describes the available merge strategies in ObjectMerger. Each section provides a description, a full example (inputs, definition, result), and a Java code snippet demonstrating usage.

## Available Strategies

- [Standard Strategy](#standard-strategy)
- [Sum Strategy](#sum-strategy)
- [Concatenate Strategy](#concatenate-strategy)
- [Maximum Strategy](#maximum-strategy)
- [Minimum Strategy](#minimum-strategy)
- [Average Strategy](#average-strategy)
- [Priority Strategy](#priority-strategy)
- [Conditional Strategy](#conditional-strategy)
- [GraalJS Strategy](#graaljs-strategy)
- [MVEL Strategy](#mvel-strategy)
- [List Strategy](#list-strategy)
- [Map Strategy](#map-strategy)
- [Nested Strategy](#nested-strategy)

---

### Standard Strategy
**Strategy Name:** `standard`  
**Description:** Picks the first non-null value found in the source list. If all are null, returns the default value. This is the default strategy if none is specified.

**Example Scenario:** Merging a title field where "Legacy System" (json1) takes precedence over "New System" (json2) because json1 is first in the list.

#### Input Data
**json1**
```json
{
  "title": "Legacy System"
}
```
**json2**
```json
{
  "title": "New System"
}
```

#### Merge Definition
```json
{
  "definitions": {
    "title": {
      "strategy": "standard",
      "defaultValue": "Unknown"
    }
  }
}
```

#### Result
```json
{
  "title": "Legacy System"
}
```

#### Java Code Example
```java
StandardFieldDefinition<String> titleDef = StandardFieldDefinition.<String>builder()
    .defaultValue("Unknown")
    .build();
```

---

### Sum Strategy
**Strategy Name:** `sum`  
**Description:** Sums up numeric values from all sources.

**Example Scenario:** Calculating a total score from two sources.

#### Input Data
**json1**
```json
{
  "score": 10
}
```
**json2**
```json
{
  "score": 20
}
```

#### Merge Definition
```json
{
  "definitions": {
    "score": {
      "strategy": "sum",
      "defaultValue": 0
    }
  }
}
```

#### Result
```json
{
  "score": 30
}
```

#### Java Code Example
```java
StandardFieldDefinition<Number> scoreDef = StandardFieldDefinition.<Number>builder()
    .strategy("sum")
    .defaultValue(0)
    .build();
```

---

### Concatenate Strategy
**Strategy Name:** `concatenate`  
**Description:** Joins string values from all sources with a delimiter (default `,`).

**Example Scenario:** Merging tags from different sources.

#### Input Data
**json1**
```json
{
  "tags": "tag1"
}
```
**json2**
```json
{
  "tags": "tag2"
}
```

#### Merge Definition
```json
{
  "definitions": {
    "tags": {
      "strategy": "concatenate",
      "defaultValue": ""
    }
  }
}
```

#### Result
```json
{
  "tags": "tag1,tag2"
}
```

#### Java Code Example
```java
StandardFieldDefinition<String> tagsDef = StandardFieldDefinition.<String>builder()
    .strategy("concatenate")
    .defaultValue("") // or any delimiter
    .build();
```

---

### Maximum Strategy
**Strategy Name:** `maximum`  
**Description:** Selects the maximum value from all sources (Numbers or Comparables).

**Example Scenario:** Determining the highest access level found.

#### Input Data
**json1**
```json
{
  "level": 5
}
```
**json2**
```json
{
  "level": 8
}
```

#### Merge Definition
```json
{
  "definitions": {
    "level": {
      "strategy": "maximum",
      "defaultValue": 0
    }
  }
}
```

#### Result
```json
{
  "level": 8
}
```

#### Java Code Example
```java
StandardFieldDefinition<Number> levelDef = StandardFieldDefinition.<Number>builder()
    .strategy("maximum")
    .defaultValue(0)
    .build();
```

---

### Minimum Strategy
**Strategy Name:** `minimum`  
**Description:** Selects the minimum value from all sources.

**Example Scenario:** Finding the lowest price across vendors.

#### Input Data
**json1**
```json
{
  "price": 99.99
}
```
**json2**
```json
{
  "price": 45.50
}
```

#### Merge Definition
```json
{
  "definitions": {
    "price": {
      "strategy": "minimum",
      "defaultValue": 0.0
    }
  }
}
```

#### Result
```json
{
  "price": 45.5
}
```

#### Java Code Example
```java
StandardFieldDefinition<Number> priceDef = StandardFieldDefinition.<Number>builder()
    .strategy("minimum")
    .defaultValue(0.0)
    .build();
```

---

### Average Strategy
**Strategy Name:** `average`  
**Description:** Calculates the arithmetic mean of numeric values.

**Example Scenario:** Averaging ratings from different reviews.

#### Input Data
**json1**
```json
{
  "rating": 4.0
}
```
**json2**
```json
{
  "rating": 5.0
}
```

#### Merge Definition
```json
{
  "definitions": {
    "rating": {
      "strategy": "average",
      "defaultValue": 0.0
    }
  }
}
```

#### Result
```json
{
  "rating": 4.5
}
```

#### Java Code Example
```java
StandardFieldDefinition<Number> ratingDef = StandardFieldDefinition.<Number>builder()
    .strategy("average")
    .defaultValue(0.0)
    .build();
```

---

### Priority Strategy
**Strategy Name:** `priority`  
**Description:** Selects a value based on explicit source priority ranking. Lower number indicates higher priority.

**Example Scenario:** Publishing status where "json2" (publisher) overrides "json1" (drafter).

#### Input Data
**json1**
```json
{
  "status": "DRAFT"
}
```
**json2**
```json
{
  "status": "PUBLISHED"
}
```

#### Merge Definition
```json
{
  "definitions": {
    "status": {
      "strategy": "priority",
      "defaultValue": "UNKNOWN",
      "priority": {
        "json2": 1,
        "json1": 2
      }
    }
  }
}
```

#### Result
```json
{
  "status": "PUBLISHED"
}
```

#### Java Code Example
```java
Map<String, Integer> priorities = new HashMap<>();
priorities.put("json2", 1);
priorities.put("json1", 2);

PriorityFieldDefinition<String> statusDef = PriorityFieldDefinition.<String>builder()
    .strategy("priority")
    .defaultValue("UNKNOWN")
    .priority(priorities)
    .build();
```

---

### Conditional Strategy
**Strategy Name:** `conditional`  
**Description:** Evaluates expressions to decide which sub-strategy to use. The syntax depends on the **active extension** (MVEL or GraalJS).

**Important:** You must include **either** `objectmerger-mvel` **or** `objectmerger-graaljs` in your dependencies, but not both.

**Context Variables:** `sources` (List<LabeledSource>), `values` (Map<SourceLabel, ValueOfCurrentField>).

#### If using MVEL Extension
Requires `objectmerger-mvel`.
```json
{
  "definitions": {
    "category": {
      "strategy": "conditional",
      "defaultValue": "unknown",
      "cases": [
        {
          "condition": "values['json1'] == 'adult'",
          "useStrategy": { "strategy": "priority", "priority": {"json1": 1} }
        }
      ]
    }
  }
}
```

#### If using GraalJS Extension
Requires `objectmerger-graaljs`.
```json
{
  "definitions": {
    "category": {
      "strategy": "conditional",
      "defaultValue": "unknown",
      "cases": [
        {
          "condition": "values.get('json1') == 'adult'",
          "useStrategy": { "strategy": "priority", "priority": {"json1": 1} }
        }
      ]
    }
  }
}
```

#### Java Code Example
The Java setup is identical, only the condition string syntax varies.
```java
ConditionCase<String> adultCase = new ConditionCase<>();
// Use MVEL or GraalJS syntax depending on your dependencies
adultCase.setCondition("values.get('json1') == 'adult'"); 

Map<String, Integer> p = new HashMap<>();
p.put("json1", 1);
p.put("json2", 2);
PriorityFieldDefinition<String> priorityDef = PriorityFieldDefinition.<String>builder()
    .priority(p)
    .build();

adultCase.setUseStrategy(priorityDef);

ConditionalFieldDefinition<String> categoryDef = ConditionalFieldDefinition.<String>builder()
    .defaultValue("unknown")
    .cases(List.of(adultCase))
    .build();
```

---

### MVEL Strategy
**Strategy Name:** `mvel`  
**Description:** Executes a complex MVEL expression to calculate the value.

**Context Variables:** `sources` (Map<SourceLabel, SourceObject>).

**Example Scenario:** Calculating a final price by applying a discount rate from the same source.

#### Input Data
**json1**
```json
{
  "price": 100,
  "discount": 0.1
}
```

#### Merge Definition
```json
{
  "definitions": {
    "finalPrice": {
      "strategy": "mvel",
      "expression": "sources['json1']['price'] * (1.0 - sources['json1']['discount'])",
      "defaultValue": 0.0
    }
  }
}
```

#### Result
```json
{
  "finalPrice": 90.0
}
```

#### Java Code Example
```java
MvelFieldDefinition mvelDef = MvelFieldDefinition.builder()
    .expression("sources['json1']['price'] * (1.0 - sources['json1']['discount'])")
    .defaultValue(0.0)
    .build();
```

---

### GraalJS Strategy
**Strategy Name:** `graaljs`  
**Description:** Executes a complex JavaScript expression (via GraalVM Polyglot) to calculate the value.

**Context Variables:** `sources` (Map<SourceLabel, SourceObject>).

**Example Scenario:** Calculating a final price by applying a discount rate from the same source.

#### Input Data
**json1**
```json
{
  "price": 100,
  "discount": 0.1
}
```

#### Merge Definition
```json
{
  "definitions": {
    "finalPrice": {
      "strategy": "graaljs",
      "expression": "sources.get('json1').price * (1.0 - sources.get('json1').discount)",
      "defaultValue": 0.0
    }
  }
}
```

#### Result
```json
{
  "finalPrice": 90.0
}
```

#### Java Code Example
```java
GraalJsFieldDefinition jsDef = GraalJsFieldDefinition.builder()
    .expression("sources.get('json1').price * (1.0 - sources.get('json1').discount)")
    .defaultValue(0.0)
    .build();
```

---

### List Strategy
**Strategy Name:** `mergeList`  
**Description:** Merges list field by grouping items by an identifier field.

**Example Scenario:** Merging lists of items where items are identified by "id".

#### Input Data
**json1**
```json
{
  "items": [
    { "id": "1", "name": "Item 1" }
  ]
}
```
**json2**
```json
{
  "items": [
    { "id": "2", "name": "Item 2" }
  ]
}
```

#### Merge Definition
```json
{
  "definitions": {
    "items": {
      "strategy": "mergeList",
      "identifyBy": "id",
      "itemMergeDefinition": {
        "definitions": {
          "name": {
            "strategy": "standard",
            "defaultValue": "Unknown"
          }
        }
      }
    }
  }
}
```

#### Result
```json
{
  "items": [
    { "id": "1", "name": "Item 1" },
    { "id": "2", "name": "Item 2" }
  ]
}
```

#### Java Code Example
```java
ListFieldDefinition<List<Object>> itemsDef = ListFieldDefinition.<List<Object>>builder()
    .identifyBy("id")
    .build();

ItemMergeDefinition itemDef = new ItemMergeDefinition();
// ... configure itemDef definitions ...
itemsDef.setItemMergeDefinition(itemDef);
```

---

### Map Strategy
**Strategy Name:** `mergeMap`  
**Description:** Merges map fields by union of keys.

**Example Scenario:** merging translation maps.

#### Input Data
**json1**
```json
{
  "translations": {
    "en": "Hello"
  }
}
```
**json2**
```json
{
  "translations": {
    "de": "Hallo"
  }
}
```

#### Merge Definition
```json
{
  "definitions": {
    "translations": {
      "strategy": "mergeMap"
    }
  }
}
```

#### Result
```json
{
  "translations": {
    "en": "Hello",
    "de": "Hallo"
  }
}
```

#### Java Code Example
```java
MapFieldDefinition<Map<Object, Object>> transDef = MapFieldDefinition.<Map<Object, Object>>builder()
---

### Nested Strategy
**Strategy Name:** `nested`  
**Description:** Recursively merges nested POJO objects based on a nested definition. This allows for granular control over sub-field merging.

**Example Scenario:** Merging an address where the street comes from an API source (json2) and the zip code from a database source (json1).

#### Input Data
**json1 (db)**
```json
{
  "address": {
    "street": "Old St",
    "zip": "12345"
  }
}
```
**json2 (api)**
```json
{
  "address": {
    "street": "New St",
    "zip": "99999"
  }
}
```

#### Merge Definition
```json
{
  "definitions": {
    "address": {
      "strategy": "nested",
      "nestedDefinition": {
        "definitions": {
          "street": {
            "strategy": "priority",
            "priority": {"json2": 1, "json1": 2}
          },
          "zip": {
            "strategy": "priority",
            "priority": {"json1": 1, "json2": 2}
          }
        }
      }
    }
  }
}
```

#### Result
```json
{
  "address": {
    "street": "New St",
    "zip": "12345"
  }
}
```

#### Java Code Example
```java
NestedFieldDefinition<Address> addressDef = NestedFieldDefinition.<Address>builder()
    .nestedDefinition(nestedMergeDefinition)
    .build();
```

