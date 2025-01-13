package com.appiancorp.solutionsconsulting.plugin.mongodb.operations;

import java.util.HashMap;
import java.util.Map;

/**
 * Serves as the base class for all MongoDB operations in the plugin.
 * <p>
 * An "Operation" in this plugin represents a specific task or action performed
 * on a MongoDB database, such as querying, inserting, updating, deleting, or aggregating data.
 * The {@code BaseOperation} class provides a default implementation for request
 * diagnostics and serves as the starting point for defining custom MongoDB operations.
 * </p>
 * <p>
 * This class implements the {@link Operation} interface, ensuring that all
 * operations can provide diagnostic information through the {@link #getRequestDiagnostic()} method.
 * Subclasses of {@code BaseOperation} can extend its functionality to implement
 * more complex or operation-specific behavior.
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class BaseOperation implements Operation {

    /**
     * Provides diagnostic information for the current operation.
     * <p>
     * This method returns a default, empty {@link HashMap}, which can be used
     * as part of an integration's request diagnostics. Subclasses may override
     * this method to include operation-specific diagnostic data, such as
     * parameters or metadata relevant to the execution of the operation.
     * </p>
     *
     * @return a {@link Map} containing key-value pairs representing diagnostic data
     */
    public Map<String, Object> getRequestDiagnostic() {
        return new HashMap<>();
    }
}