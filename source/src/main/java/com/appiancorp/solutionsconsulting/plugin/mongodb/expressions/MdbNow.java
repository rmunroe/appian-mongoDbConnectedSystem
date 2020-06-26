package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;

import java.sql.Timestamp;
import java.util.Date;

@MongoDbCategory
public class MdbNow {

    @Function
    public Timestamp mdbNow() {
        return new Timestamp((new Date()).getTime());
    }
}
