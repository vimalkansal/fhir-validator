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

## Technical Stack

- Java 21
- Apache Camel 4.3.0
- HAPI FHIR 6.10.3
- SLF4J for logging

## Validation Process

The application implements a multi-layered validation approach:

1. **Initial JSON Parsing**
   - Validates basic JSON syntax
   - Checks for required FHIR elements (e.g., 'resourceType')
   - Ensures JSON structure matches FHIR format

2. **FHIR Resource Validation**
   - Uses HAPI FHIR's validation framework
   - Validates against FHIR R4B resource definitions
   - Components:
     - DefaultProfileValidationSupport: Base FHIR validation
     - InMemoryTerminologyServerValidationSupport: Code system validation
     - CommonCodeSystemsTerminologyService: Common terminology validation

3. **Specific Validations**
   - Resource structure validation
   - Required fields checking
   - Data type validation
   - Code system value validation
   - Reference integrity checking

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
│       └── invalid/  # Output directory for invalid resources
```

## How to Use

### Prerequisites
- Java 21 or higher
- Maven 3.8 or higher

### Building the Application
```bash
mvn clean package
```

### Running the Application
```bash
java -jar target/fhir-validator-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### File Processing
1. Place FHIR JSON resources in `src/main/resources/input`
2. The application will automatically:
   - Process each file
   - Validate the FHIR resource
   - Move valid resources to `src/main/resources/valid`
   - Move invalid resources to `src/main/resources/invalid`

## Sample Resources

The application includes test resources that demonstrate various validation scenarios:

1. **Valid Resources**
   - `valid-patient.json`: Complete Patient resource
   - `valid-observation.json`: Complete Observation resource

2. **Invalid Resources**
   - `invalid-patient-missing-type.json`: Missing resourceType
   - `invalid-patient-wrong-gender.json`: Invalid gender code
   - `invalid-observation-missing-status.json`: Missing required status
   - `malformed-json.json`: Invalid JSON syntax

## Validation Details

### Validation Chain

The validator uses a chain of validation supports:

```java
ValidationSupportChain validationSupport = new ValidationSupportChain(
    new DefaultProfileValidationSupport(fhirContext),
    new InMemoryTerminologyServerValidationSupport(fhirContext),
    new CommonCodeSystemsTerminologyService(fhirContext)
);
```

### Error Categories

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

### Validation Results

The processor provides detailed validation results including:
- Success/failure status
- Error messages with locations
- Error counts
- Warning messages
- Information messages

## Apache Camel Route

The application uses Apache Camel for file processing:

```java
from("file:src/main/resources/input?noop=true")
    .routeId("fhir-validation")
    .process(new FhirValidationProcessor())
    .choice()
        .when(header("validation-passed").isEqualTo(true))
            .to("file:src/main/resources/valid")
        .otherwise()
            .to("file:src/main/resources/invalid");
```

## Configuration

The validator can be configured with different options:

```java
FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
instanceValidator.setAnyExtensionsAllowed(true);  // More lenient with extensions
instanceValidator.setErrorForUnknownProfiles(false);  // Don't error on unknown profiles
```

## Error Handling

The application implements comprehensive error handling:
- JSON parsing errors
- FHIR validation errors
- File system errors
- Resource type mismatches

## Logging

Uses SLF4J for structured logging with different levels:
- INFO: Processing information, successful validations
- WARN: Non-critical issues (e.g., missing value sets)
- ERROR: Validation failures, processing errors

## Future Enhancements

Possible improvements include:
1. Support for additional FHIR versions
2. Implementation Guide validation
3. Custom validation rules
4. Validation reports in different formats
5. Web interface for validation
6. Batch processing statistics
7. Custom terminology services integration

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.