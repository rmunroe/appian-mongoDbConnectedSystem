package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

public class InvalidDictionaryException extends Exception {

    private String errorCode = "InvalidField";

    public InvalidDictionaryException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidDictionaryException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return this.errorCode;
    }
}
