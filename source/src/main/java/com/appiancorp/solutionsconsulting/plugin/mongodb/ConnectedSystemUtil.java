package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidJsonException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidMongoOperationException;
import com.mongodb.internal.build.MongoDriverVersion;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * Utility class for managing request and response diagnostics, timing, and
 * building Appian {@link IntegrationResponse} objects in the context of MongoDB
 * connected systems. This class tracks execution times, populates diagnostic
 * data, and constructs success or error responses (including handling of
 * custom exception types).
 *
 * <p>Usage pattern:
 * <ul>
 *   <li>Create a new instance specifying the method in use (e.g. "GET" or "INSERT").</li>
 *   <li>Call {@link #startTiming()} before execution and {@link #stopTiming()} afterward to measure duration.</li>
 *   <li>Add request or response diagnostics and custom data via the provided <code>add*</code> methods.</li>
 *   <li>Use {@link #buildSuccess()} to return a successful {@link IntegrationResponse}, or
 *       {@link #buildApiExceptionError(Exception)} to handle exception cases.</li>
 * </ul>
 * </p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
@SuppressWarnings("unused")
public class ConnectedSystemUtil {
    private Long startTime;
    private Long endTime;
    private final HashMap<String, Object> requestDiagnostics;
    private final HashMap<String, Object> responseDiagnostics;
    private final HashMap<String, Object> response;

    /**
     * Constructs a new {@code ConnectedSystemUtil} instance and initializes the
     * request/response diagnostics with the provided method name and current
     * MongoDB driver version.
     *
     * @param method The method name (e.g., "GET", "INSERT", etc.) used for logging.
     */
    public ConnectedSystemUtil(String method) {
        requestDiagnostics = new HashMap<>();
        responseDiagnostics = new HashMap<>();
        responseDiagnostics.put("MongoDB Java Sync Driver Version", MongoDriverVersion.VERSION);
        responseDiagnostics.put("Method Used", method);
        response = new HashMap<>();
    }

    /**
     * Records the current time in milliseconds as the start time.
     */
    public void startTiming() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Records the current time in milliseconds as the end time.
     */
    public void stopTiming() {
        endTime = System.currentTimeMillis();
    }

    /**
     * Retrieves the measured time (in milliseconds) between
     * {@link #startTiming()} and {@link #stopTiming()}.
     *
     * @return the elapsed time in milliseconds, or -1 if timing was not started or stopped.
     */
    public long getTiming() {
        if (startTime == null || endTime == null) {
            return -1;
        }
        return endTime - startTime;
    }

    /**
     * Adds a single entry to the request diagnostics map.
     *
     * @param key   the diagnostic key
     * @param value the diagnostic value
     */
    public void addRequestDiagnostic(String key, Object value) {
        requestDiagnostics.put(key, value);
    }

    /**
     * Adds all key-value pairs from the provided map to the request diagnostics.
     *
     * @param map a map of diagnostic data to add
     */
    public void addAllRequestDiagnostic(Map<String, Object> map) {
        requestDiagnostics.putAll(map);
    }

    /**
     * Retrieves the request diagnostics map.
     *
     * @return a {@link HashMap} containing diagnostic data for the request
     */
    public HashMap<String, Object> getRequestDiagnostics() {
        return requestDiagnostics;
    }

    /**
     * Adds a single entry to the response diagnostics map.
     *
     * @param key   the diagnostic key
     * @param value the diagnostic value
     */
    public void addResponseDiagnostic(String key, Object value) {
        responseDiagnostics.put(key, value);
    }

    /**
     * Adds all key-value pairs from the provided map to the response diagnostics.
     *
     * @param map a map of diagnostic data to add
     */
    public void addAllResponseDiagnostic(Map<String, Object> map) {
        responseDiagnostics.putAll(map);
    }

    /**
     * Retrieves the response diagnostics map.
     *
     * @return a {@link HashMap} containing diagnostic data for the response
     */
    public HashMap<String, Object> getResponseDiagnostics() {
        return responseDiagnostics;
    }

    /**
     * Adds a single entry to the response map, which can be part of the
     * final payload returned in the Appian integration.
     *
     * @param key   the response data key
     * @param value the response data value
     */
    public void addResponse(String key, Object value) {
        response.put(key, value);
    }

    /**
     * Adds all key-value pairs from the provided map to the response map.
     *
     * @param map a map of response data to add
     */
    public void addAllResponse(Map<String, Object> map) {
        response.putAll(map);
    }

    /**
     * Builds an {@link IntegrationResponse} for a successful execution, including
     * diagnostic information (request and response) and execution timing.
     *
     * @return a success-type {@link IntegrationResponse}
     */
    public IntegrationResponse buildSuccess() {
        IntegrationResponse.Builder integrationResponseBuilder = IntegrationResponse.forSuccess(getResponse());
        IntegrationDesignerDiagnostic integrationDesignerDiagnostic = IntegrationDesignerDiagnostic.builder()
                .addRequestDiagnostic(getRequestDiagnostics())
                .addResponseDiagnostic(getResponseDiagnostics())
                .addExecutionTimeDiagnostic(getTiming())
                .build();
        return integrationResponseBuilder.withDiagnostic(integrationDesignerDiagnostic).build();
    }

    /**
     * Retrieves the response data map.
     *
     * @return a {@link HashMap} representing response data for this integration
     */
    public HashMap<String, Object> getResponse() {
        return response;
    }

    /**
     * Builds an {@link IntegrationResponse} representing an error encountered
     * during execution (e.g., invalid JSON, invalid MongoDB operation, or a
     * generic exception).
     *
     * @param e the {@link Exception} thrown during the execution
     * @return an error-type {@link IntegrationResponse} containing diagnostic info
     */
    public IntegrationResponse buildApiExceptionError(Exception e) {
        if (e instanceof InvalidJsonException) {
            return buildApiExceptionError(
                    "Invalid Json Exception",
                    e.getMessage(),
                    ((InvalidJsonException) e).getJsonString()
            );
        } else if (e instanceof InvalidMongoOperationException) {
            return buildApiExceptionError(
                    "Invalid Bulk Write Operation",
                    "An invalid operation was provided in the list of operations JSON.",
                    e.getMessage()
            );
        } else {
            String message = e.getMessage();
            String detail = "";
            if (message.contains("The full response is")) {
                detail = message.replaceAll("^.* The full response is ", "");
                message = message.replaceAll(" The full response is .*$", "");
            }
            if (detail.isEmpty()) {
                detail = message;
                message = "Something went wrong - " + e.getClass().getName();
            }
            return buildApiExceptionError(
                    String.join(" ", StringUtils.splitByCharacterTypeCamelCase(e.getClass().getSimpleName())),
                    message,
                    detail
            );
        }
    }

    /**
     * Builds an {@link IntegrationResponse} representing an error, to be used when
     * additional context (title, message, detail) is known. This also constructs
     * and attaches an {@link IntegrationDesignerDiagnostic} for troubleshooting.
     *
     * @param title   a short, descriptive title for the error
     * @param message the error message
     * @param detail  additional detail relevant to the error
     * @return an error-type {@link IntegrationResponse} containing diagnostic info
     */
    public IntegrationResponse buildApiExceptionError(String title, String message, String detail) {
        IntegrationDesignerDiagnostic diagnostics = IntegrationDesignerDiagnostic.builder()
                .addRequestDiagnostic(requestDiagnostics)
                .addResponseDiagnostic(responseDiagnostics)
                .build();
        IntegrationError integrationError = IntegrationError.builder()
                .title(title)
                .message(message)
                .detail(detail)
                .build();
        IntegrationResponse.Builder integrationResponseBuilder = IntegrationResponse.forError(integrationError)
                .withDiagnostic(diagnostics);
        return integrationResponseBuilder.build();
    }
}