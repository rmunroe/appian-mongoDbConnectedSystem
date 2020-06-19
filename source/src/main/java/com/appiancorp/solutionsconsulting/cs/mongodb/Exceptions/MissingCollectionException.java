package com.appiancorp.solutionsconsulting.cs.mongodb.Exceptions;

public class MissingCollectionException extends Exception {
    public MissingCollectionException(String collectionName) {
        super("Collection '" + collectionName + "' does not exist.");
    }
}
