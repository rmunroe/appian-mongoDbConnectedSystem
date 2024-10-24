package com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions;

public class InvalidMongoOperationException extends Exception{
    public String detail;

    public InvalidMongoOperationException(String detail) {
        super("Invalid operation provided. "+detail);
    }

    public InvalidMongoOperationException() {
        super("Invalid operation provided. Supported operations are insertOne, deleteOne, replaceOne, updateOne, deleteMany and updateMany.");
    }
}
