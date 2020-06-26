package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

public class MissingCollectionException extends Exception {
    public MissingCollectionException(String collectionName) {
        super("Collection '" + collectionName + "' does not exist.");
    }
}
