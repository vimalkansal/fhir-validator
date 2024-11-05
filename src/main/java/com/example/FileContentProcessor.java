package com.example;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.apache.camel.component.file.GenericFile;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileContentProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(FileContentProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        @SuppressWarnings("unchecked")
        GenericFile<File> genericFile = exchange.getIn().getBody(GenericFile.class);
        
        if (genericFile == null) {
            throw new IllegalArgumentException("No file found in the exchange");
        }

        File file = genericFile.getFile();
        logger.debug("Reading file: {}", file.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(file)) {
            String content = IOUtils.toString(fis, StandardCharsets.UTF_8);
            logger.debug("File content length: {}", content.length());
            exchange.getMessage().setBody(content);
        }
    }
}