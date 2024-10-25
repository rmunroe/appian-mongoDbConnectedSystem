package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Field {
    @Function
    public String m_Field(@Parameter String field, @Parameter String... queryClauses) {
        if (queryClauses.length == 1)
            if (!queryClauses[0].matches("^\\s*\\{.*}$") && // is not enclosed in braces
                    !queryClauses[0].matches("^[0-9.]$") && // is not a number
                    !queryClauses[0].equals("true") &&            // is not boolean true
                    !queryClauses[0].equals("false")              // is not boolean false
            ) {
                queryClauses[0] = "\"" + queryClauses[0] + "\"";
            }

        return "\"" + field + "\": " + String.join(", ", queryClauses);
    }
}