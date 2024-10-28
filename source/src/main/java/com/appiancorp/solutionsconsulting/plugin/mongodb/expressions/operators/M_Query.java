package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Query {
    @Function
    public String m_Query(@Parameter String... queryClauses) throws Exception {
        String clauses = String.join(", ", queryClauses)
                .replaceAll("^\\s*", "")
                .replaceAll("\\s*$", "");

        if (MongoDbJsonHelper.isValidJson(clauses))
            return clauses;
        else {
            clauses = "{ " + clauses + " }";
            if (MongoDbJsonHelper.isValidJson(clauses))
                return clauses;
            else
                throw new Exception("Invalid JSON");
        }
    }
}