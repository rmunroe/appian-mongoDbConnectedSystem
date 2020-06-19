package com.appiancorp.solutionsconsulting.cs.mongodb.operations;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all data objects for executing operations
 */
public class BaseOperation implements Operation {
    /**
     * Returns a HashMap suitable for using as the Integration's Request Diagnostics
     *
     * @return Map<String, Object>
     */
    public Map<String, Object> getRequestDiagnostic() {
        return new HashMap<>();
    }
}
