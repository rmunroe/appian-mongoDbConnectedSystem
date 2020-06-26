package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

public class InvalidFieldException extends Exception {

    private String errorCode = "InvalidField";

    public InvalidFieldException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InvalidFieldException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return this.errorCode;
    }


}
