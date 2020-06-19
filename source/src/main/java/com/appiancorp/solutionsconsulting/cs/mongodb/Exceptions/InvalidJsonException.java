package com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions;

public class InvalidJsonException extends Exception {

    public String jsonString;

    public InvalidJsonException(String message, String jsonString) {
        super(message);

        this.jsonString = jsonString;
    }
}
