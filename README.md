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

## FHIR Validation Details

### 1. Resource Structure Validation
- **Basic JSON Validation**
  ```json
  {
    "resourceType": "Patient",  // Required field
    "id": "example",
    // ... other fields
  }
  ```
  Error if missing resourceType:
  ```
  HAPI-1838: Invalid JSON content detected, missing required element: 'resourceType'
  ```

### 2. Required Fields Validation
- **Example: Observation Status**
  ```json
  {
    "resourceType": "Observation",
    "status": "final",  // Required field
    // ... other fields
  }
  ```
  Error if missing:
  ```
  Observation.status: minimum required = 1, but only found 0
  ```

### 3. Terminology Validation
- **Example: Patient Gender**
  ```json
  {
    "resourceType": "Patient",
    "gender": "male"  // Must be from AdministrativeGender value set
  }
  ```
  Error for invalid value:
  ```
  HAPI-1821: [element="gender"] Invalid attribute value "invalid-gender": Unknown AdministrativeGender code
  ```

### 4. Data Type Validation
- **Example: Date Format**
  ```json
  {
    "birthDate": "1970-01-01"  // Must be valid date format
  }
  ```

### 5. Reference Validation
- **Example: Observation Subject**
  ```json
  {
    "subject": {
      "reference": "Patient/example"  // Must be valid reference format
    }
  }
  ```

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

## MainApp - Application Entry Point

The `MainApp` class serves as the primary entry point for the FHIR Validator application. It handles initialization, test data generation, and Camel route configuration.

### Key Features

1. **Environment Initialization**
   - Creates required directories (`input`, `output`, `error`)
   - Cleans existing files to ensure fresh validation runs
   - Generates sample FHIR resources for testing

2. **Test Data Generation**
   Automatically generates sample FHIR resources including:
   - Valid Patient resource
   - Valid Observation resource
   - Invalid Patient (missing resourceType)
   - Invalid Patient (wrong gender code)
   - Invalid Observation (missing required status)
   - Malformed JSON resource

## Sample Resources and Validation Examples

### 1. Valid Patient Resource
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

### 2. Valid Observation Resource
```json
{
  "resourceType": "Observation",
  "id": "blood-pressure",
  "status": "final",
  "code": {
    "coding": [{
      "system": "http://loinc.org",
      "code": "55284-4",
      "display": "Blood Pressure"
    }]
  },
  "subject": {
    "reference": "Patient/example"
  }
}
```

### 3. Invalid Resources with Validation Errors
```json
{
  "id": "example",     // Error: Missing resourceType
  "name": [{
    "use": "official",
    "family": "Doe",
    "given": ["John"]
  }]
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

## Validation Process Flow

1. **File Detection**
   - Apache Camel monitors input directory
   - Picks up new FHIR resource files

2. **Initial Parsing**
   - Validates JSON syntax
   - Checks basic FHIR structure

3. **FHIR Validation**
   - Resource structure validation
   - Required fields validation
   - Terminology validation
   - Reference validation

4. **Result Processing**
   - Valid resources moved to valid directory
   - Invalid resources moved to invalid directory with error details
   - Error logging and reporting

## Error Categories and Examples

1. **Structure Errors**
   ```
   HAPI-1838: Invalid JSON content detected, missing required element: 'resourceType'
   ```

2. **Code System Errors**
   ```
   HAPI-1821: [element="gender"] Invalid attribute value "invalid-gender": Unknown AdministrativeGender code
   ```

3. **Required Field Errors**
   ```
   Observation.status: minimum required = 1, but only found 0
   ```

4. **Syntax Errors**
   ```
   HAPI-1861: Failed to parse JSON encoded FHIR content: Unexpected character
   ```

## Logging

Uses SLF4J for structured logging with different levels:
- INFO: Processing information, successful validations
- WARN: Non-critical issues (e.g., missing value sets)
- ERROR: Validation failures, processing errors
- DEBUG: Detailed processing information

## Future Enhancements

1. Support for additional FHIR versions
2. Implementation Guide validation
3. Custom validation rules
4. Validation reports in different formats
5. Web interface for validation
6. Batch processing statistics
7. Custom terminology services integration
8. Support for custom profiles and extensions
9. Enhanced validation reporting with severity levels

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.