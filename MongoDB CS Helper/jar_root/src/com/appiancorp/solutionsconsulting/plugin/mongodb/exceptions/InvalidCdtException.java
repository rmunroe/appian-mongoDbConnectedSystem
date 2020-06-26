package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

public class InvalidCdtException extends Exception {

    private String errorCode = "InvalidCdt";

    public InvalidCdtException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidCdtException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return this.errorCode;
    }

}
