package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.Map;

/**
 * Represents a data Operation in the MongoDB plugin.
 *
 * <p>
 * An "Operation" in this plugin represents a specific task or action performed
 * on a MongoDB database, such as querying, inserting, updating, deleting, or aggregating data.
 * The {@code BaseOperation} class provides a default implementation for request
 * diagnostics and serves as the starting point for defining custom MongoDB operations.
 * </p>
 *
 * <p>
 * This interface defines a contract for classes that need to provide
 * diagnostic information related to MongoDB operations.
 * Implementing classes should use the {@link #getRequestDiagnostic()} method
 * to supply key-value pairs representing diagnostic data for a specific operation.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public interface Operation {

    /**
     * Retrieves diagnostic information for the operation.
     * <p>
     * The returned map contains key-value pairs describing details of
     * the operation's execution, such as parameters, configurations, or metadata.
     * </p>
     *
     * @return a {@link Map} where the keys are {@link String} identifiers for diagnostic
     *         data and the values are {@link Object} representations of the associated data
     */
    Map<String, Object> getRequestDiagnostic();
}