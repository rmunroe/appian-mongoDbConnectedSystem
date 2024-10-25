package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Query {
    @Function
    public String m_Query(@Parameter String... queryClauses) {
        String clauses = String.join(", ", queryClauses)
                .replaceAll("^\\s*", "")
                .replaceAll("\\s*$", "");

        if (!clauses.startsWith("{") && !clauses.endsWith("}"))
            return "{ " + clauses + " }";
        else
            return clauses;
    }
}