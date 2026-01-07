# ObjectMerger

Eine Java-Library zum intelligenten Zusammenführen von Objektdaten aus mehreren Quellen (z.B. verschiedene Datenbanken, APIs, externe Services). Mit konfigurierbare Merge-Strategien können Sie festlegen, welche Datenquelle Vorrang hat oder wie Daten kombiniert werden.

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

### 2. Maximum-Wert-Strategie
Nutzt den höchsten numerischen Wert aus allen Quellen:
```json
{
  "age": {
    "strategy": "maximum",
    "defaultValue": 0
  }
}
```

### 3. Listen-Merge-Strategie
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
    "email": {
      "priority": {
        "crm": 1,
        "database": 2
      }
    },
    "age": {
      "strategy": "maximum"
    }
  }
}
```

### 2. Objekte zusammenführen

```java
// Quellen laden
Person dbPerson = loadFromDatabase();
Person crmPerson = loadFromCRM();

// Merge-Definition aus JSON laden
MergeDefinition definition = loadMergeDefinition();

// Objekte mergen
Person mergedPerson = ObjectMerger.merge(
    Person.class,
    definition,
    new LabeledSource<>("database", dbPerson),
    new LabeledSource<>("crm", crmPerson)
);

// Ergebnis nutzen
System.out.println("Name: " + mergedPerson.getName());
System.out.println("Email: " + mergedPerson.getEmail());
System.out.println("Age: " + mergedPerson.getAge());
```

## Projektstruktur

```
src/
├── main/java/de/x132/
│   ├── ObjectMerger.java                    # Hauptklasse - Merging-Logik
│   ├── MergeDefinition.java                 # Container für Feld-Definitionen
│   ├── FieldDefinition.java                 # Definition für einzelne Felder
│   ├── ItemMergeDefinition.java             # Definition für Listen-Items
│   ├── LabeledSource.java                   # Quelle mit Label (z.B. "database", "crm")
│   └── strategy/
│       ├── MergeStrategy.java               # Interface für alle Strategien
│       ├── PriorityMergeStrategy.java       # Prioritätsbasierte Auswahl (Standard)
│       ├── MaximumValueStrategy.java        # Findet Maximum-Wert
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
        ├── MaximumValueStrategyTest.java
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

## Version

- **Aktuell**: 0.1.0-SNAPSHOT
- **Java**: 21 LTS
- **License**: Siehe LICENSE (falls vorhanden)

## Beispiel-Daten

Im `src/test/resources/` Verzeichnis finden Sie Beispiel-JSON-Dateien:
- `person/` - Beispiele für einfaches Merge-Szenario (3 Quellen: analytics, crm, database)
- `family/` - Beispiele für Listen-Merge-Szenario
