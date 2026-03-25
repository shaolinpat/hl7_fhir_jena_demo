package com.shaolinpat.hl7fhirjena;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for loading RDF/Turtle data into a Jena Model.
 *
 * Supports:
 *   - loadTurtle()        filesystem path (production use)
 *   - loadFromClasspath() classpath resource (test fixtures)
 */
public class RdfLoader {

    /**
     * Load a Turtle file from the filesystem.
     *
     * @param filePath path to a .ttl file
     * @return populated Jena Model
     * @throws IllegalArgumentException if the file does not exist
     * @throws RuntimeException if parsing fails
     */
    public static Model loadTurtle(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }
        Model model = ModelFactory.createDefaultModel();
        try {
            RDFDataMgr.read(model, filePath, Lang.TURTLE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Turtle file: " + filePath, e);
        }
        return model;
    }

    /**
     * Load a Turtle file from the classpath.
     * Used primarily for test fixtures under src/test/resources.
     *
     * @param classpathResource path relative to classpath root, e.g. "data/valid_patient.ttl"
     * @return populated Jena Model
     * @throws IllegalArgumentException if the resource is not found
     * @throws RuntimeException if parsing fails
     */
    public static Model loadFromClasspath(String classpathResource) {
        InputStream in = RdfLoader.class.getClassLoader()
                .getResourceAsStream(classpathResource);
        if (in == null) {
            throw new IllegalArgumentException(
                "Classpath resource not found: " + classpathResource);
        }
        Model model = ModelFactory.createDefaultModel();
        try {
            RDFDataMgr.read(model, in, Lang.TURTLE);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to parse Turtle from classpath: " + classpathResource, e);
        }
        return model;
    }
}