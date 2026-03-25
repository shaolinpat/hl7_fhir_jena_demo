package com.shaolinpat.hl7fhirjena;

import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

class SparqlRunnerTest {

    private Model model;

    @BeforeEach
    void setUp() {
        // Reuse the same fixture from RdfLoaderTest
        model = RdfLoader.loadFromClasspath("data/valid_patient.ttl");
    }

    @Test
    void selectReturnsRowsForValidQuery() {
        String query =
            "PREFIX hft: <https://w3id.org/shaolinpat/hft#> " +
            "SELECT ?patient WHERE { ?patient a hft:Patient . }";
        int rows = SparqlRunner.runSelect(model, query);
        assertTrue(rows > 0, "Expected at least one result row");
    }

    @Test
    void selectFromClasspathReturnsRows() {
        int rows = SparqlRunner.runSelectFromClasspath(
            model, "queries/patient_summary.rq");
        assertTrue(rows > 0, "Expected at least one result row from classpath query");
    }

    @Test
    void throwsOnMissingQueryResource() {
        assertThrows(IllegalArgumentException.class, () ->
            SparqlRunner.runSelectFromClasspath(model, "queries/does_not_exist.rq")
        );
    }

    @Test
    void readQueryStringThrowsRuntimeExceptionOnReadFailure() throws Exception {
        InputStream mockStream = mock(InputStream.class);
        when(mockStream.readAllBytes()).thenThrow(new IOException("simulated read failure"));

        assertThrows(RuntimeException.class, () ->
            SparqlRunner.readQueryString(mockStream, "test")
                );
    }
}