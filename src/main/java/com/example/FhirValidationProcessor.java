package com.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.parser.IParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FhirValidationProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(FhirValidationProcessor.class);
    private final FhirContext fhirContext;
    private final FhirValidator validator;
    private final IParser jsonParser;

    public FhirValidationProcessor() {
        this.fhirContext = FhirContext.forR4B();
        this.validator = fhirContext.newValidator();
        this.jsonParser = fhirContext.newJsonParser();
        
        // Create a validation chain
        ValidationSupportChain validationSupport = new ValidationSupportChain(
            new DefaultProfileValidationSupport(fhirContext),
            new InMemoryTerminologyServerValidationSupport(fhirContext),
            new CommonCodeSystemsTerminologyService(fhirContext)
        );

        // Configure instance validator
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
        instanceValidator.setAnyExtensionsAllowed(true);  // More lenient with extensions
        instanceValidator.setErrorForUnknownProfiles(false);  // Don't error on unknown profiles
        
        // Register validator
        validator.registerValidatorModule(instanceValidator);
        
        try {
            Files.createDirectories(Paths.get("src/main/resources/valid"));
            Files.createDirectories(Paths.get("src/main/resources/invalid"));
            logger.info("✅ Initialized directory structure and validation support");
        } catch (Exception e) {
            logger.error("Error managing directories: {}", e.getMessage());
        }
    }

    private record ValidationIssue(String severity, String location, String message) {}

    @Override
    public void process(Exchange exchange) throws Exception {
        GenericFile<File> file = exchange.getIn().getBody(GenericFile.class);
        String content = new String(Files.readAllBytes(file.getFile().toPath()), StandardCharsets.UTF_8);
        String filename = exchange.getIn().getHeader("CamelFileName", String.class);
        
        logger.info("Processing file: {}", filename);

        try {
            // Parse FHIR resource
            IBaseResource resource = jsonParser.parseResource(content);
            logger.info("Parsed resource type: {}", resource.fhirType());

            // Validate
            ValidationResult results = validator.validateWithResult(resource);
            
            // Collect issues by severity
            List<ValidationIssue> errors = new ArrayList<>();
            List<ValidationIssue> warnings = new ArrayList<>();
            List<ValidationIssue> info = new ArrayList<>();
            
            for (SingleValidationMessage message : results.getMessages()) {
                ValidationIssue issue = new ValidationIssue(
                    message.getSeverity().name(),
                    message.getLocationString(),
                    message.getMessage()
                );
                
                switch (message.getSeverity()) {
                    case ERROR, FATAL -> errors.add(issue);
                    case WARNING -> warnings.add(issue);
                    case INFORMATION -> info.add(issue);
                }
            }
            
            if (errors.isEmpty()) {
                logger.info("✅ Valid {} resource: {}", resource.fhirType(), filename);
                if (!warnings.isEmpty()) {
                    logger.info("⚠️  Warnings:");
                    warnings.forEach(w -> logger.info("  - {}: {}", w.location(), w.message()));
                }
                if (!info.isEmpty()) {
                    logger.info("ℹ️  Information:");
                    info.forEach(i -> logger.info("  - {}: {}", i.location(), i.message()));
                }
                exchange.getMessage().setHeader("validation-passed", true);
                exchange.getMessage().setBody(content);
            } else {
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append(String.format("❌ Invalid %s resource: %s\n", resource.fhirType(), filename));
                errorMsg.append("Validation Messages:\n");
                errors.forEach(e -> errorMsg.append(String.format("❌ %s: %s\n", e.location(), e.message())));
                
                logger.error(errorMsg.toString());
                exchange.getMessage().setHeader("validation-passed", false);
                exchange.getMessage().setHeader("validation-error", errorMsg.toString());
                exchange.getMessage().setHeader("validation-error-count", errors.size());
                exchange.getMessage().setHeader("validation-warning-count", warnings.size());
                exchange.getMessage().setBody(content);
            }

        } catch (Exception e) {
            logger.error("❌ Error processing file {}: {}", filename, e.getMessage());
            exchange.getMessage().setHeader("validation-passed", false);
            exchange.getMessage().setHeader("validation-error", e.getMessage());
            exchange.getMessage().setBody(content);
            throw e;
        }
    }
}