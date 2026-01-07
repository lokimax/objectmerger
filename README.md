# ObjectMerger

Eine Java-Library zum intelligenten Zusammenführen von Objektdaten aus mehreren Quellen (z.B. verschiedene Datenbanken, APIs, externe Services). Mit konfigurierbaren Merge-Strategien können Sie festlegen, welche Datenquelle Vorrang hat oder wie Daten kombiniert werden.

## Anwendungsbeispiele

- **Datenverschmelzung**: Konsolidierung von Kundendaten aus CRM, Datenbank und Analytics
- **Multi-Source-Integration**: Zusammenführung von Informationen aus mehreren APIs oder Services
- **Konfliktauflösung**: Bestimmung, welche Quelle bei abweichenden Daten verwendet wird
- **Datenlisten-Merging**: Kombination von Listen aus verschiedenen Quellen

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

### 5. Listen-Merge-Strategie
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

```bash
# Projekt bauen und Tests ausführen
mvn clean test

# Nur JAR-Datei erstellen
mvn clean package

# Tests kompilieren und ausführen
mvn test
```

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
```

## Projektstruktur

```
src/
├── main/java/de/x132/
│   ├── ObjectMerger.java                    # Hauptklasse - Merging-Logik
│   ├── MergeDefinition.java                 # Container für Feld-Definitionen
│   ├── FieldDefinition.java                 # Definition für einzelne Felder
│   ├── ItemMergeDefinition.java             # Definition für Listen-Items
│   ├── LabeledSource.java                   # Quelle mit Label (z.B. "database", "api")
│   └── strategy/
│       ├── MergeStrategy.java               # Interface für alle Strategien
│       ├── PriorityMergeStrategy.java       # Prioritätsbasierte Auswahl (Standard)
│       ├── MinimumValueStrategy.java        # Findet Minimum-Wert
│       ├── MaximumValueStrategy.java        # Findet Maximum-Wert
│       ├── AverageValueStrategy.java        # Berechnet Durchschnittswert
│       └── ListMergeStrategy.java           # Mergt Listen von Objekten
└── test/java/
    ├── person/
    │   ├── Person.java                      # Test-Modell
    │   └── MultiSourceMergeTest.java        # Integration-Test
    ├── family/
    │   ├── Family.java                      # Test-Modell für Listen-Merging
    │   ├── FamilyMember.java
    │   └── ListMergeTest.java               # Listen-Merge-Test
    └── strategy/
        ├── PriorityMergeStrategyTest.java
        ├── MinimumValueStrategyTest.java
        ├── MaximumValueStrategyTest.java
        ├── AverageValueStrategyTest.java
        └── ListMergeStrategyTest.java
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

## Merge-Strategien im Detail

| Strategie | Nutzung | Beispiel |
|-----------|---------|---------|
| `priority` (Standard) | Wählt Wert aus Quelle mit höchster Priorität | Name: Database (1) > CRM (2) > Analytics (3) |
| `minimum` | Verwendet kleinsten numerischen Wert | Price: Min aus [100, 50, 75] = 50 |
| `maximum` | Verwendet größten numerischen Wert | Age: Max aus [25, 35, 30] = 35 |
| `average` | Berechnet Durchschnitt numerischer Werte | Rating: Average aus [4.5, 3.5, 4.0] = 4.0 |
| `mergeList` | Kombiniert Listen basierend auf ID | Merge [Item1, Item2] + [Item1, Item3] |

## Version

- **Aktuell**: 0.1.0-SNAPSHOT
- **Java**: 21 LTS
- **License**: Siehe LICENSE (falls vorhanden)

## Beispiel-Daten

Im `src/test/resources/` Verzeichnis finden Sie Beispiel-JSON-Dateien:
- `person/` - Beispiele für einfaches Merge-Szenario (3 Quellen: analytics, crm, database)
- `family/` - Beispiele für Listen-Merge-Szenario
