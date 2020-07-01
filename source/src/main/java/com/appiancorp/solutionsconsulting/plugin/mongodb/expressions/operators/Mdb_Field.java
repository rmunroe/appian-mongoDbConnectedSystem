package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class Mdb_Field {
    @Function
    public String mdb_Field(@Parameter String field, @Parameter String queryClause) {
        return "{ \"" + field + "\": " + queryClause + " }";
    }
}