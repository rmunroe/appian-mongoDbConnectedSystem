package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

public class InvalidJsonException extends Exception {

    public String jsonString;

    public InvalidJsonException(String message, String jsonString) {
        super(message);

        this.jsonString = jsonString;
    }
}
