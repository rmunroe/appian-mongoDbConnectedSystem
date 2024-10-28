package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Field {
    @Function
    public String m_Field(@Parameter String field, @Parameter String... queryClauses) {
        return "\"" + field + "\": { " + String.join(", ", queryClauses) + " }";
    }
}