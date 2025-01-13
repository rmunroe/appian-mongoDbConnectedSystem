package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

/**
 * Thrown to indicate that an invalid MongoDB operation was requested or provided.
 *
 * @author Vuram SWAT
 * @since 1.4
 */
public class InvalidMongoOperationException extends Exception{

    /**
     * Constructs a new {@code InvalidMongoOperationException} with a detailed message
     * that includes the invalid operation provided.
     *
     * @param detail the invalid operation or additional details about the error
     */
    public InvalidMongoOperationException(String detail) {
        super("Invalid operation provided. " + detail);
    }

    /**
     * Constructs a new {@code InvalidMongoOperationException} with a default message
     * listing the supported operations.
     */
    public InvalidMongoOperationException() {
        super("Invalid operation provided. Supported operations are insertOne, deleteOne, replaceOne, updateOne, deleteMany and updateMany.");
    }
}