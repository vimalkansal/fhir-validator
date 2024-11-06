# FHIR Resource Validator

A Java application using Apache Camel and HAPI FHIR to validate FHIR resources. The validator processes JSON FHIR resources, performs comprehensive validation, and sorts them into valid/invalid directories based on the validation results.

## Features

- Validates FHIR R4B resources against standard FHIR definitions
- Performs structural validation using HAPI FHIR validators
- Validates terminology and code systems
- Handles multiple types of validation:
  - JSON structure and syntax
  - Required fields
  - Data type constraints
  - Code system values
  - Reference validation
- Processes files in batch using Apache Camel file component
- Provides detailed validation feedback with error locations and messages
- Separates valid and invalid resources into different directories
- Generates sample FHIR resources for testing
- Auto-creates required directory structure

## Technical Stack

- Java 21
- Maven 3.8.x (Build and dependency management)
- Apache Camel 4.3.0
- HAPI FHIR 6.10.3
- SLF4J 2.0.9 (Logging framework)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           ├── MainApp.java                 # Application entry point
│   │           ├── FhirValidationProcessor.java # FHIR validation logic
│   │           └── FhirValidationRoute.java     # Camel route definition
│   └── resources/
│       ├── input/    # Input directory for FHIR resources
│       ├── valid/    # Output directory for valid resources
│       ├── invalid/  # Output directory for invalid resources
│       ├── error/    # Directory for processing errors
│       └── output/   # General output directory
```

## FHIR Validation Implementation

### Current Validation Support Chain
```java
ValidationSupportChain validationSupport = new ValidationSupportChain(
    new DefaultProfileValidationSupport(fhirContext),
    new InMemoryTerminologyServerValidationSupport(fhirContext),
    new CommonCodeSystemsTerminologyService(fhirContext)
);
```

#### 1. DefaultProfileValidationSupport
- Base FHIR specification validation
- Structure definitions validation
- Resource constraints checking
- Example validation:
  ```json
  {
    "resourceType": "Patient",
    "identifier": [{
      "system": "http://example.org/fhir/ids",
      "value": "12345"
    }]
  }
  ```

#### 2. InMemoryTerminologyServerValidationSupport
- Code system validation
- Value set membership checking
- Example validation:
  ```json
  {
    "gender": "male"  // Validates against AdministrativeGender code system
  }
  ```

#### 3. CommonCodeSystemsTerminologyService
- Standard terminology validation
- Common code systems support
- Example validation:
  ```json
  {
    "status": "final",  // Validates ObservationStatus
    "code": {
      "coding": [{
        "system": "http://loinc.org",
        "code": "55284-4"
      }]
    }
  }
  ```

### FHIR Specification Validation Types

#### 1. Structure Validation
- Resource type validation
- Required elements checking
- Data type conformance

#### 2. Profile Validation
- Base resource profiles
- Custom profiles
- Implementation guide conformance

#### 3. Business Rule Validation
- Invariants
- Cross-field validations
- Conditional requirements

#### 4. Terminology Validation
- Code systems (SNOMED CT, LOINC, RxNorm)
- Value sets
- Concept maps

#### 5. Reference Validation
- Resource references
- Contained resources
- Reference resolution

### Missing Validations & Enhancement Opportunities

#### 1. Profile-based Validation
```java
// Enhancement example
ValidationResult results = validator.validateWithResult(resource, 
    "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient");
```

#### 2. Reference Validation
```java
public boolean validateReferences(IBaseResource resource) {
    List<String> references = // Extract references from resource
    for (String reference : references) {
        // Validate reference exists
        // Check reference format
    }
}
```

#### 3. Business Rule Validation
```java
public class CustomBusinessRuleValidator {
    public ValidationResult validate(IBaseResource resource) {
        // Custom validation rules
        // Organization-specific requirements
    }
}
```

#### 4. Units of Measure Validation
```java
private void validateQuantities(Observation obs) {
    // UCUM validation
    // Value range checking
    // Unit consistency
}
```

## Building and Running

### Prerequisites
- Java 21
- Maven 3.8.x or higher
- Git (for version control)

### Build Commands
```bash
# Clean and build the project
mvn clean install

# Run the application
java -jar target/fhir-validator-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Sample Resources

### Valid Patient Resource
```json
{
  "resourceType": "Patient",
  "id": "example",
  "identifier": [{
    "system": "http://example.org/fhir/ids",
    "value": "12345"
  }],
  "active": true,
  "name": [{
    "use": "official",
    "family": "Doe",
    "given": ["John"]
  }],
  "gender": "male",
  "birthDate": "1970-01-01"
}
```

### Valid Observation Resource
```json
{
  "resourceType": "Observation",
  "status": "final",
  "code": {
    "coding": [{
      "system": "http://loinc.org",
      "code": "55284-4",
      "display": "Blood Pressure"
    }]
  }
}
```

## Sample Run Output

```
[main] INFO com.example.MainApp - Starting FHIR validation service...

# Processing Invalid Patient (Missing ResourceType)
[INFO] Processing file: invalid-patient-missing-type.json
[ERROR] ❌ Error: Invalid JSON content detected, missing required element: 'resourceType'

# Processing Valid Observation
[INFO] Processing file: valid-observation.json
[INFO] ✅ Valid Observation resource: valid-observation.json

# Processing Invalid Observation (Missing Status)
[ERROR] ❌ Invalid Observation resource: invalid-observation-missing-status.json
Validation Messages:
❌ Observation: Observation.status: minimum required = 1, but only found 0
```

## Future Enhancements

1. Profile Validation Support
   - US Core Profile validation
   - Custom profile support
   - Implementation guide validation

2. Enhanced Reference Validation
   - Reference existence checking
   - Reference type validation
   - Circular reference detection

3. Business Rule Framework
   - Custom validation rules
   - Organization-specific requirements
   - Conditional validation support

4. Extended Terminology Support
   - Additional code systems
   - Value set expansion
   - Terminology service integration

5. Reporting and Analytics
   - Validation statistics
   - Error pattern analysis
   - Batch processing reports

6. Web Interface
   - Interactive validation
   - Real-time feedback
   - Validation result visualization