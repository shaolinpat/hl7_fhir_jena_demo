package com.shaolinpat.hl7fhirjena;

import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;

import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.Lang;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;


class RdfLoaderTest {

    @Test
    void loadsValidTurtleFromClasspath() {
        Model model = RdfLoader.loadFromClasspath("data/valid_patient.ttl");
        assertNotNull(model);
        assertTrue(model.size() > 0, "Model should contain at least one triple");
    }

    @Test
    void throwsOnMissingClasspathResource() {
        assertThrows(IllegalArgumentException.class, () ->
                RdfLoader.loadFromClasspath("data/does_not_exist.ttl")
        );
    }

    @Test
    void loadTurtleLoadsValidFile(@TempDir Path tempDir) throws Exception {
        // Write a valid Turtle file to a temp location on the filesystem
        Path ttl = tempDir.resolve("test.ttl");
        Files.writeString(ttl,
                "@prefix hft: <https://w3id.org/shaolinpat/hft#> .\n" +
                        "hft:Patient_p999 a hft:Patient ; hft:familyName \"Smith\" .\n"
        );
        Model model = RdfLoader.loadTurtle(ttl.toString());
        assertNotNull(model);
        assertTrue(model.size() > 0);
    }

    @Test
    void LoadTurtleThrowsOnMissingFIle() {
        assertThrows(IllegalArgumentException.class, () ->
                RdfLoader.loadTurtle("/tmp/does_not_exist_xyz.ttl")
        );
    }

    @Test
    void loadFromClassPathThrowsOnMalformedTurtle() {
        assertThrows(RuntimeException.class, () ->
                RdfLoader.loadFromClasspath("data/malformed.ttl")
        );
    }

    @Test
    void loadTurtleThrowsRuntimeExceptionOnParseFailure(@TempDir Path tempDir) throws Exception {
        Path ttl = tempDir.resolve("test.ttl");
        Files.writeString(ttl,
                "@prefix hft: <https://w3id.org/shaolinpat/hft#> .\n" +
                        "hft:Patient_p9999 a hft:Patient .\n"
        );

        try (MockedStatic<RDFDataMgr> mocked = mockStatic(RDFDataMgr.class)) {
            mocked.when(() -> RDFDataMgr.read(any(org.apache.jena.rdf.model.Model.class), anyString(), eq(Lang.TURTLE)))
                    .thenThrow(new RuntimeException("simulated parse failure"));

            assertThrows(RuntimeException.class, () ->
                    RdfLoader.loadTurtle(ttl.toString())
            );
        }
    }
}