package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

/**
 * Thrown when a provided JSON string is determined to be invalid or cannot be parsed.
 *
 * @author Rob Munroe
 * @since 1.0
 */
public class InvalidJsonException extends Exception {

    private String jsonString;

    /**
     * Constructs a new {@code InvalidJsonException} with the specified detail message
     * and the invalid JSON string that triggered this exception.
     *
     * @param message     the detail message explaining what went wrong
     * @param jsonString  the invalid JSON string that caused this exception
     */
    public InvalidJsonException(String message, String jsonString) {
        super(message);
        this.setJsonString(jsonString);
    }

    /**
     * The invalid JSON string that led to this exception being thrown.
     */
    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}