package com.shaolinpat.hl7fhirjena;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Executes SPARQL SELECT queries against a Jena Model.
 *
 * Supports loading queries from:
 *   - the classpath (src/main/resources or src/test/resources)
 *   - a query string passed directly
 */
public class SparqlRunner {

    private static final Logger log = LoggerFactory.getLogger(SparqlRunner.class);

    /**
     * Execute a SPARQL SELECT query string against a model.
     * Prints results to stdout.
     *
     * @param model       Jena Model to query
     * @param queryString SPARQL SELECT query as a string
     * @return number of result rows
     */
    public static int runSelect(Model model, String queryString) {
        Query query = QueryFactory.create(queryString);
        int rowCount = 0;

        try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qe.execSelect();
            rowCount = printResults(results);
        }

        log.info("Query returned {} row(s)", rowCount);
        return rowCount;
    }

    /**
     * Load a SPARQL query from the classpath and execute it against a model.
     * Prints results to stdout.
     *
     * @param model             Jena Model to query
     * @param classpathResource path relative to classpath root, e.g., "queries/patient_summary.rq"
     * @return number of result rows
     * @throws IllegalArgumentException if the resource is not found
     * @throws RuntimeException if the query cannot be read or executed
     */
    public static int runSelectFromClasspath(Model model, String classpathResource) {
        log.info("Loading SPARQL query from classpath: {}", classpathResource);
        String queryString = loadQueryString(classpathResource);
        return runSelect(model, queryString);
    }

    /**
     * Load a SPARQL query string from the classpath.
     *
     * @param classpathResource path relative to classpath root
     * @return query as a String
     * @throws IllegalArgumentException if the resource is not found
     * @throws RuntimeException if the resource cannot be read
     */
    public static String loadQueryString(String classpathResource) {
        InputStream in = SparqlRunner.class.getClassLoader()
                .getResourceAsStream(classpathResource);
        if (in == null) {
            throw new IllegalArgumentException(
                "Classpath resource not found: " + classpathResource);
        }
        return readQueryString(in, classpathResource);
    }

    /**
     * Read a SPARQL query string from an InputStream.
     * Extracted to enable testing of the IOException path.
     *
     * @param in    InputStream to read from
     * @param label label for error messages (typically the classpath resource name)
     * @return query as a String
     * @throws RuntimeException if the stream cannot be read
     */
    static String readQueryString(InputStream in, String label) {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to read query from classpath: " + label, e);
        }
    }

    /**
     * Buffer a ResultSet into a list, print rows to stdout, and return the row count.
     *
     * @param results ResultSet from a SELECT query
     * @return number of rows printed
     */
    private static int printResults(ResultSet results) {
        // Buffer results so we can both count and print
        List<QuerySolution> rows = new java.util.ArrayList<>();
        while (results.hasNext()) {
            rows.add(results.next());
        }
        int rowCount = rows.size();

        // Print header and rows
        System.out.println("Results (" + rowCount + " row(s)):");
        for (QuerySolution row : rows) {
            System.out.println("  " + row);
        }
        return rowCount;
    }
}