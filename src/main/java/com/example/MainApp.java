package com.example;

import org.apache.camel.main.Main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) throws Exception {
        // Initialize directories and test data
        initializeEnvironment();
        
        // Start Camel
        Main main = new Main();
        main.configure().addRoutesBuilder(new FhirValidationRoute());
        logger.info("Starting FHIR validation service...");
        main.run();
    }

    private static void initializeEnvironment() throws IOException {
        // Create or clean directories
        Path inputDir = Paths.get("src", "main", "resources", "input");
        Path outputDir = Paths.get("src", "main", "resources", "output");
        Path errorDir = Paths.get("src", "main", "resources", "error");

        // Create directories if they don't exist
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);
        Files.createDirectories(errorDir);

        // Clean existing files
        cleanDirectory(inputDir);
        cleanDirectory(outputDir);
        cleanDirectory(errorDir);

        logger.info("Directories initialized and cleaned");

        // Generate test data
        generateTestData(inputDir);
        logger.info("Test data generated in input directory");
    }

    private static void cleanDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                files.forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        logger.error("Error deleting file: " + file, e);
                    }
                });
            }
        }
    }

    private static void generateTestData(Path inputDir) throws IOException {
        // Valid Patient
        String validPatient = """
        {
          "resourceType": "Patient",
          "id": "example",
          "text": {
            "status": "generated",
            "div": "<div xmlns=\\"http://www.w3.org/1999/xhtml\\">John Doe</div>"
          },
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
          "birthDate": "1970-01-01",
          "address": [{
            "use": "home",
            "line": ["123 Main St"],
            "city": "Anywhere",
            "state": "CA",
            "postalCode": "90210",
            "country": "USA"
          }]
        }
        """;

        // Valid Observation
        String validObservation = """
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
          },
          "effectiveDateTime": "2023-12-25T08:30:00+01:00",
          "valueQuantity": {
            "value": 120,
            "unit": "mmHg",
            "system": "http://unitsofmeasure.org",
            "code": "mm[Hg]"
          }
        }
        """;

        // Invalid Patient (Missing Required resourceType)
        String invalidPatient1 = """
        {
          "id": "example",
          "name": [{
            "use": "official",
            "family": "Doe",
            "given": ["John"]
          }],
          "gender": "male"
        }
        """;

        // Invalid Patient (Wrong Gender)
        String invalidPatient2 = """
        {
          "resourceType": "Patient",
          "id": "example",
          "name": [{
            "use": "official",
            "family": "Smith",
            "given": ["Jane"]
          }],
          "gender": "invalid-gender",
          "birthDate": "1980-01-01"
        }
        """;

        // Invalid Observation (Missing Status)
        String invalidObservation = """
        {
          "resourceType": "Observation",
          "id": "heart-rate",
          "code": {
            "coding": [{
              "system": "http://loinc.org",
              "code": "8867-4",
              "display": "Heart rate"
            }]
          },
          "subject": {
            "reference": "Patient/example"
          },
          "valueQuantity": {
            "value": 80,
            "unit": "beats/minute",
            "system": "http://unitsofmeasure.org",
            "code": "/min"
          }
        }
        """;

        // Malformed JSON
        String malformedJson = """
        {
          "resourceType": "Patient"
          "id": "malformed-example"
          "name": [{
            "use": "official"
            "family": "Doe",
            "given": ["John"]
          }]
        }
        """;

        // Write all test files
        writeFile(inputDir, "valid-patient.json", validPatient);
        writeFile(inputDir, "valid-observation.json", validObservation);
        writeFile(inputDir, "invalid-patient-missing-type.json", invalidPatient1);
        writeFile(inputDir, "invalid-patient-wrong-gender.json", invalidPatient2);
        writeFile(inputDir, "invalid-observation-missing-status.json", invalidObservation);
        writeFile(inputDir, "malformed-json.json", malformedJson);
    }

    private static void writeFile(Path dir, String filename, String content) throws IOException {
        Path filePath = dir.resolve(filename);
        Files.writeString(filePath, content);
        logger.info("Generated test file: {}", filename);
    }
}