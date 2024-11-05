package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.LoggingLevel;

public class FhirValidationRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // Error Handler
        onException(Exception.class)
            .handled(true)
            .log(LoggingLevel.ERROR, "Error processing file ${header.CamelFileName}: ${exception.message}")
            .to("file:src/main/resources/invalid");

        // Main Route
        from("file:src/main/resources/input?noop=true")
            .routeId("fhir-validation")
            .log("Processing: ${header.CamelFileName}")
            // No type conversion here - let the processor handle it
            .process(new FhirValidationProcessor())
            .choice()
                .when(header("validation-passed").isEqualTo(true))
                    .log("✅ Valid FHIR message: ${header.CamelFileName}")
                    .to("file:src/main/resources/valid")
                .otherwise()
                    .log("❌ Invalid FHIR message: ${header.CamelFileName}")
                    .log("Error: ${header.validation-error}")
                    .to("file:src/main/resources/invalid");
    }
}