package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.logical;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Not {
    @Function
    public String m_Not(@Parameter String queryClause) {
        return "\"$not\": { " + queryClause + " }";
    }
}