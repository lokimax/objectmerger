# YAML Support

## Overview

The ObjectMerger Spring Boot application now supports YAML format in addition to JSON for merge requests.

## Endpoints

### JSON Endpoint (Original)
- **URL**: `POST /api/v1/merge`
- **Content-Type**: `application/json`
- **Accept**: `application/json`

### YAML Endpoint (New)
- **URL**: `POST /api/v1/merge/yaml`
- **Content-Type**: `application/x-yaml`
- **Accept**: `application/x-yaml`

## Usage Examples

### JSON Request
```bash
curl -X POST http://localhost:8080/api/v1/merge \
  -H "Content-Type: application/json" \
  -d '{
    "targetClass": "de.x132.objectmerger.model.Person",
    "definition": {
      "name": {"priority": {"database": 1, "crm": 2, "analytics": 3}},
      "age": {"strategy": "maximum", "defaultValue": 0}
    },
    "sources": [
      {"label": "database", "data": {"name": "Max", "age": 30}},
      {"label": "crm", "data": {"name": "Maximilian", "age": 25}}
    ]
  }'
```

### YAML Request
```bash
curl -X POST http://localhost:8080/api/v1/merge/yaml \
  -H "Content-Type: application/x-yaml" \
  -H "Accept: application/x-yaml" \
  --data-binary @example-merge-request.yaml
```

**Example YAML file** (`example-merge-request.yaml`):
```yaml
targetClass: "de.x132.objectmerger.model.Person"
definition:
  name:
    priority:
      database: 1
      crm: 2
      analytics: 3
  age:
    strategy: "maximum"
    defaultValue: 0
  email:
    priority:
      database: 1
      crm: 2
      analytics: 3
  phone:
    priority:
      analytics: 1
      crm: 2
      database: 3
sources:
  - label: "database"
    data:
      name: "Max Müller"
      age: 30
      email: "max@example.com"
      phone: null
  - label: "crm"
    data:
      name: "Maximilian Müller"
      age: 25
      email: null
      phone: "030-123456"
  - label: "analytics"
    data:
      name: null
      age: 35
      email: "max.mueller@example.de"
      phone: "030-654321"
```

### YAML Response
```yaml
---
name: "Max Müller"
age: 35
email: "max@example.com"
phone: "030-654321"
```

## Implementation Details

### Dependencies
Added Jackson YAML support in `pom.xml`:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
</dependency>
```

### Configuration
- `YamlConfiguration.java` - Configures YAML message converters
- Uses `YAMLMapper` from Jackson for YAML serialization/deserialization
- Content negotiation set up for `application/x-yaml` media type

### Key Points
- Both JSON and YAML endpoints use the same underlying ObjectMerger logic
- No changes needed to the core ObjectMerger library (it works with POJOs)
- YAML files are parsed into the same Java objects as JSON
- Response format matches the request format (YAML in, YAML out)

## Benefits
- More human-readable format for complex merge definitions
- Better suited for configuration files
- Supports comments (unlike JSON)
- Easier to maintain large merge configurations
