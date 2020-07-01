package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;

import java.sql.Timestamp;
import java.util.Date;

@MongoDbCategory
public class Mdb_Now {

    @Function
    public Timestamp mdb_Now() {
        return new Timestamp((new Date()).getTime());
    }
}
