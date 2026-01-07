# ObjectMerger

Eine Java-Library zum intelligenten Zusammenführen von Objektdaten aus mehreren Quellen (z.B. verschiedene Datenbanken, APIs, externe Services). Mit konfigurierbaren Merge-Strategien können Sie festlegen, welche Datenquelle Vorrang hat oder wie Daten kombiniert werden.

**Multi-Module Maven Projekt mit:**
- **objectmerger**: Kern-Library mit Merge-Logik und 8 Strategien
- **objectmerger-cli**: Kommandozeilen-Tool für JSON-Merging
- **objectmerger-spring-boot**: REST API mit Swagger/OpenAPI-Dokumentation

## Anwendungsbeispiele

- **Datenverschmelzung**: Konsolidierung von Kundendaten aus CRM, Datenbank und Analytics
- **Multi-Source-Integration**: Zusammenführung von Informationen aus mehreren APIs oder Services
- **Konfliktauflösung**: Bestimmung, welche Quelle bei abweichenden Daten verwendet wird
- **Datenlisten-Merging**: Kombination von Listen aus verschiedenen Quellen
- **String-Konkatenation**: Zusammenfügen von Text-Werten mit Prioritätsreihenfolge

## Features

### 1. Prioritätsbasierte Auswahl (Standard)
Wählt den Wert aus der Quelle mit höchster Priorität:
```json
{
  "name": {
    "priority": {
      "database": 1,
      "crm": 2,
      "analytics": 3
    }
  }
}
```

### 2. Minimum-Wert-Strategie
Nutzt den kleinsten numerischen Wert aus allen Quellen:
```json
{
  "minimumPrice": {
    "strategy": "minimum",
    "defaultValue": 0
  }
}
```

### 3. Maximum-Wert-Strategie
Nutzt den höchsten numerischen Wert aus allen Quellen:
```json
{
  "maximumAge": {
    "strategy": "maximum",
    "defaultValue": 0
  }
}
```

### 4. Durchschnittswert-Strategie
Berechnet den Durchschnitt aus allen numerischen Werten:
```json
{
  "averageRating": {
    "strategy": "average",
    "defaultValue": 0
  }
}
```

### 5. Summen-Strategie
Addiert alle numerischen Werte aus allen Quellen:
```json
{
  "totalRevenue": {
    "strategy": "sum",
    "defaultValue": 0
  }
}
```

### 6. Concatenate-Strategie
Vereinigt mehrere String-Werte zu einem (respektiert Priorität und ignoriert null-Werte):
```json
{
  "tags": {
    "strategy": "concatenate",
    "priority": {
      "primary": 1,
      "secondary": 2,
      "tertiary": 3
    },
    "defaultValue": ""
  }
}
```

### 7. Listen-Merge-Strategie
Kombiniert Listen aus mehreren Quellen basierend auf Identifikationsmerkmale:
```json
{
  "items": {
    "strategy": "mergeList",
    "identifyBy": "id",
    "itemMergeDefinition": { ... }
  }
}
```

### 8. Map-Merge-Strategie
Vereinigt Map-Objekte aus mehreren Quellen, gruppiert nach Key:
```json
{
  "settings": {
    "strategy": "mergeMap",
    "itemMergeDefinition": { ... }
  }
}
```

## Anforderungen

- **Java**: 21 (LTS)
- **Maven**: 3.8+

## Installation

Fügen Sie folgende Dependency zu Ihrem `pom.xml` hinzu:
```xml
<dependency>
    <groupId>de.x132</groupId>
    <artifactId>objectmerger</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Build & Test

Das Projekt ist als Multi-Module Maven Projekt strukturiert:

```bash
# Gesamtes Projekt bauen (alle Module)
mvn clean install

# Nur Tests ausführen
mvn test

# Build ohne Tests
mvn clean install -DskipTests

# Einzelnes Modul bauen
mvn -pl objectmerger clean install
mvn -pl objectmerger-cli clean package
mvn -pl objectmerger-spring-boot clean package
```

Build-Reihenfolge:
1. `objectmerger` - Core Library
2. `objectmerger-cli` - CLI Tool (hängt von objectmerger ab)
3. `objectmerger-spring-boot` - REST API (hängt von objectmerger ab)

## Schnelstart

### 1. Merge-Definition erstellen

```json
{
  "definitions": {
    "name": {
      "priority": {
        "database": 1,
        "crm": 2
      }
    },
    "minPrice": {
      "strategy": "minimum"
    },
    "maxPrice": {
      "strategy": "maximum"
    },
    "avgRating": {
      "strategy": "average"
    },
    "tags": {
      "strategy": "concatenate"
    }
  }
}
```

### 2. Objekte zusammenführen

```java
// Quellen laden
Product product1 = loadFromDatabase();
Product product2 = loadFromAPI();

// Merge-Definition aus JSON laden
MergeDefinition definition = loadMergeDefinition();

// Objekte mergen
Product merged = ObjectMerger.merge(
    Product.class,
    definition,
    new LabeledSource<>("database", product1),
    new LabeledSource<>("api", product2)
);

// Ergebnis nutzen
System.out.println("Name: " + merged.getName());
System.out.println("Min Price: " + merged.getMinPrice());
System.out.println("Max Price: " + merged.getMaxPrice());
System.out.println("Avg Rating: " + merged.getAvgRating());
System.out.println("Tags: " + merged.getTags());
```

## Projektstruktur

```
objectmerger/                              # Parent POM - Multi-Module Projekt
├── pom.xml                               # Parent POM mit Dependency Management
├── objectmerger/                         # Core Library Modul
│   ├── pom.xml                          # Core Library POM
│   └── src/
│       ├── main/java/de/x132/
│       │   ├── ObjectMerger.java        # Hauptklasse - Merging-Logik
│       │   ├── MergeDefinition.java     # Container für Feld-Definitionen
│       │   ├── FieldDefinition.java     # Definition für einzelne Felder
│       │   ├── ItemMergeDefinition.java # Definition für Listen-Items
│       │   ├── LabeledSource.java       # Quelle mit Label
│       │   └── strategy/
│       │       ├── MergeStrategy.java
│       │       ├── PriorityMergeStrategy.java
│       │       ├── MinimumValueStrategy.java
│       │       ├── MaximumValueStrategy.java
│       │       ├── AverageValueStrategy.java
│       │       ├── SumValueStrategy.java
│       │       ├── ConcatenateStrategy.java
│       │       ├── ListMergeStrategy.java
│       │       └── MapMergeStrategy.java
│       └── test/java/                   # 62 Unit Tests
├── objectmerger-cli/                     # CLI Tool Modul
│   ├── pom.xml                          # CLI POM mit Picocli
│   ├── README.md                        # CLI-spezifische Dokumentation
│   └── src/
│       └── main/java/de/x132/cli/
│           ├── ObjectMergerCli.java     # Picocli Command
│           └── Person.java              # Demo-Model
└── objectmerger-spring-boot/            # REST API Modul
    ├── pom.xml                          # Spring Boot POM
    ├── README.md                        # REST API Dokumentation
    └── src/
        ├── main/java/de/x132/objectmerger/
        │   ├── ObjectMergerApplication.java
        │   ├── controller/MergeController.java
        │   ├── service/ObjectMergerService.java
        │   ├── dto/                     # Request/Response DTOs
        │   ├── model/Person.java
        │   └── util/MergeDefinitionConverter.java
        └── test/java/                   # 21 Integration Tests
```

## Erweiterte Konfiguration

### Standard-Wert setzen
```json
{
  "fieldName": {
    "priority": { "source1": 1 },
    "defaultValue": "Unbekannt"
  }
}
```

### Listen mit Identifikation mergen
```json
{
  "members": {
    "strategy": "mergeList",
    "identifyBy": "id",
    "itemMergeDefinition": {
      "definitions": {
        "name": { "priority": { "source1": 1 } },
        "age": { "strategy": "maximum" }
      }
    }
  }
}
```

### Strings mit Priorität concatenaten
```json
{
  "description": {
    "strategy": "concatenate",
    "priority": {
      "primary_source": 1,
      "secondary_source": 2
    }
  }
}
```

## Merge-Strategien im Detail

| Strategie | Nutzung | Typ | Beispiel |
|-----------|---------|-----|---------|
| `priority` (Standard) | Wählt Wert aus Quelle mit höchster Priorität | Beliebig | Name: Database (1) > CRM (2) |
| `minimum` | Verwendet kleinsten numerischen Wert | Number | Price: Min aus [100, 50, 75] = 50 |
| `maximum` | Verwendet größten numerischen Wert | Number | Age: Max aus [25, 35, 30] = 35 |
| `average` | Berechnet Durchschnitt numerischer Werte | Number | Rating: Average aus [4.5, 3.5, 4.0] = 4.0 |
| `sum` | Addiert alle numerischen Werte | Number | Sales: Sum aus [1500, 2300, 800] = 4600 |
| `concatenate` | Vereinigt Strings mit Prioritätsreihenfolge | String | Tags: "java,spring,boot" |
| `mergeList` | Kombiniert Listen basierend auf ID | List | Merge Items basierend auf ID |
| `mergeMap` | Vereinigt Maps nach Key-Gruppierung | Map | Settings: Key-basiertes Merging |

## Strategie-Anforderungen

- **Priority**: Beliebige Typen
- **Minimum/Maximum**: Numeric Typen (Integer, Long, Double, Float, BigDecimal)
- **Average**: Numeric Typen
- **Sum**: Numeric Typen
- **Concatenate**: String nur
- **MergeList**: List Typ mit Items
- **MergeMap**: Map Typ mit beliebigen Key-Value-Typen

## Version

- **Aktuell**: 0.1.0-SNAPSHOT
- **Java**: 21 LTS
- **Maven**: 3.8+
- **License**: MIT

## Module

| Modul | Beschreibung | Artifact |
|-------|-------------|----------|
| `objectmerger` | Core Library mit Merge-Logik | `de.x132:objectmerger:0.1.0-SNAPSHOT` |
| `objectmerger-cli` | Kommandozeilen-Tool (Picocli) | `de.x132:objectmerger-cli:0.1.0-SNAPSHOT` |
| `objectmerger-spring-boot` | REST API mit Swagger UI | `de.x132:objectmerger-spring-boot:0.1.0-SNAPSHOT` |

## Test-Abdeckung

- **objectmerger**: 62 Unit Tests (alle Strategien)
- **objectmerger-spring-boot**: 21 Integration Tests (Controller, Service, Converter)
- **Gesamt**: 83 Tests

## Beispiel-Daten

Im `objectmerger/src/test/resources/` Verzeichnis finden Sie Beispiel-JSON-Dateien:
- `person/` - Beispiele für einfaches Merge-Szenario (3 Quellen: analytics, crm, database)
- `family/` - Beispiele für Listen-Merge-Szenario
- `sales/` - Beispiele für Sum-Strategie (4 Quellen: amazon, ebay, shopify, woocommerce)

---

## CLI-Modul: objectmerger-cli

Eine Kommandozeilen-Anwendung zum Mergen von JSON-Dateien.

### Build

```bash
# Gesamtes Projekt bauen (baut automatisch CLI mit)
mvn clean install

# Nur CLI-Modul bauen (setzt voraus, dass objectmerger installiert ist)
mvn -pl objectmerger-cli clean package
```

Das erzeugte JAR ist ein fat JAR (~732 KB) mit allen Dependencies: `objectmerger-cli/target/objectmerger-cli-0.1.0-SNAPSHOT.jar`

### Nutzung

```bash
# Beispiel: Person aus drei Quellen mergen
java -jar objectmerger-cli/target/objectmerger-cli-0.1.0-SNAPSHOT.jar \
  --target-class de.x132.cli.Person \
  --definition objectmerger-cli/src/main/resources/person/person-merge-definition.json \
  --source database=objectmerger-cli/src/main/resources/person/db-person.json \
  --source crm=objectmerger-cli/src/main/resources/person/crm-person.json \
  --source analytics=objectmerger-cli/src/main/resources/person/analytics-person.json

# Mit Ausgabe in Datei
java -jar objectmerger-cli/target/objectmerger-cli-0.1.0-SNAPSHOT.jar \
  -t de.x132.cli.Person \
  -d objectmerger-cli/src/main/resources/person/person-merge-definition.json \
  -s database=objectmerger-cli/src/main/resources/person/db-person.json \
  -s crm=objectmerger-cli/src/main/resources/person/crm-person.json \
  -s analytics=objectmerger-cli/src/main/resources/person/analytics-person.json \
  -o merged-person.json
```

**Parameter:**
- `--target-class` (`-t`): Vollqualifizierter Klassenname des Ziel-Objekts
- `--definition` (`-d`): Pfad zur Merge-Definitions-JSON
- `--source` (`-s`): Mehrfach wiederholbar; Format `label=pfad/zur.json`
- `--output` (`-o`): Optionaler Pfad für die Ausgabedatei (ansonsten stdout)

---

## Spring Boot Modul: objectmerger-spring-boot

Eine REST API mit vollständiger Swagger/OpenAPI-Dokumentation zum interaktiven Mergen über HTTP.

### Build

```bash
# Gesamtes Projekt bauen (baut automatisch Spring Boot App mit)
mvn clean install

# Nur Spring Boot Modul bauen
mvn -pl objectmerger-spring-boot clean package
```

Das erzeugte JAR ist ein executable Spring Boot JAR (~25 MB) mit embedded Tomcat: `objectmerger-spring-boot/target/objectmerger-spring-boot-0.1.0-SNAPSHOT.jar`

### Start

```bash
# Direkt aus dem JAR starten
java -jar objectmerger-spring-boot/target/objectmerger-spring-boot-0.1.0-SNAPSHOT.jar

# Oder mit Maven
mvn -pl objectmerger-spring-boot spring-boot:run
```

App startet auf **http://localhost:8080**

### Swagger UI

Öffne im Browser: **http://localhost:8080/swagger-ui.html**

Dort kannst du:
- Alle Endpoints und ihre Schema anschauen
- Requests direkt aus der UI senden (Try it out)
- Response-Beispiele sehen

### REST-Endpoints

#### 1. Health Check
```bash
GET http://localhost:8080/api/v1/merge/health
```
Response: `{"status":"UP"}`

#### 2. Merge mit Custom Quellen (JSON)

**Endpoint:** `POST /api/v1/merge`

```bash
curl -X POST http://localhost:8080/api/v1/merge \
  -H "Content-Type: application/json" \
  -d '{
    "targetClass": "de.x132.objectmerger.model.Person",
    "definition": {
      "name": {"priority": {"database": 1, "crm": 2, "analytics": 3}},
      "age": {"strategy": "maximum", "defaultValue": 0},
      "email": {"priority": {"database": 1, "crm": 2, "analytics": 3}},
      "phone": {"priority": {"analytics": 1, "crm": 2, "database": 3}}
    },
    "sources": [
      {"label": "database", "data": {"name": "Max Müller", "age": 30, "email": "max@example.com", "phone": null}},
      {"label": "crm", "data": {"name": "Maximilian Müller", "age": 25, "email": null, "phone": "030-123456"}},
      {"label": "analytics", "data": {"name": null, "age": 35, "email": "max.mueller@example.de", "phone": "030-654321"}}
    ]
  }'
```

Response:
```json
{
  "name": "Max Müller",
  "age": 35,
  "email": "max@example.com",
  "phone": "030-654321"
}
```

#### 2b. Merge mit Custom Quellen (YAML)

**Endpoint:** `POST /api/v1/merge/yaml`

Die REST API unterstützt YAML zusätzlich zu JSON. YAML ist besser lesbar für komplexe Merge-Definitionen und unterstützt Kommentare.

```bash
curl -X POST http://localhost:8080/api/v1/merge/yaml \
  -H "Content-Type: application/x-yaml" \
  -H "Accept: application/x-yaml" \
  --data-binary @example-merge-request.yaml
```

**Beispiel YAML-Datei** (`example-merge-request.yaml`):
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

Response (YAML):
```yaml
---
name: "Max Müller"
age: 35
email: "max@example.com"
phone: "030-654321"
```

**YAML-Vorteile:**
- Menschenlesbareres Format für komplexe Merge-Definitionen
- Besser geeignet für Konfigurationsdateien
- Unterstützt Kommentare (im Gegensatz zu JSON)
- Einfachere Wartung großer Merge-Konfigurationen

#### 3. Person-Beispiel (vordefiniert)

```bash
curl -X POST http://localhost:8080/api/v1/merge/example
```

Merged automatisch drei Person-Objekte (database, crm, analytics) nach den Regeln in der Merge-Definition.

### Spring Boot Module-Struktur

```
objectmerger-spring-boot/
├── pom.xml                              # Spring Boot 3.2.1 + Springdoc OpenAPI 2.1.0 + Jackson YAML
├── README.md                            # Detaillierte API-Dokumentation
└── src/
    ├── main/
    │   ├── java/de/x132/objectmerger/
    │   │   ├── ObjectMergerApplication.java    # Spring Boot Entry Point
    │   │   ├── controller/
    │   │   │   └── MergeController.java        # REST-Controller (/api/v1/merge + /yaml)
    │   │   ├── service/
    │   │   │   └── ObjectMergerService.java    # Wrapper um ObjectMerger
    │   │   ├── config/
    │   │   │   └── YamlConfiguration.java      # YAML Message Converter
    │   │   ├── model/
    │   │   │   └── Person.java                 # Demo Model
    │   │   ├── dto/
    │   │   │   ├── MergeRequest.java           # API Request Schema
    │   │   │   └── LabeledSourceDTO.java       # Source DTO
    │   │   └── util/
    │   │       └── MergeDefinitionConverter.java  # Map→MergeDefinition
    │   └── resources/
    │       └── application.properties          # Port 8080, Swagger Config
    └── test/java/                              # 21 Integration Tests
        ├── controller/MergeControllerIntegrationTest.java  # 10 Tests
        ├── service/ObjectMergerServiceIntegrationTest.java # 4 Tests
        └── util/MergeDefinitionConverterTest.java          # 7 Tests
```

### Swagger Annotations

Die API ist vollständig mit OpenAPI 3.0 Annotations dokumentiert:
- `@Tag` für API-Gruppen
- `@Operation` für Endpoint-Beschreibungen
- `@ApiResponse` für Response-Codes
- `@Schema` für DTO-Felder

Dies generiert automatisch die Swagger UI und OpenAPI JSON unter `/api-docs`.
