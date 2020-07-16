package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.element;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Exists {
    @Function
    public String m_Exists(@Parameter Boolean value) {
        return MongoDbJsonHelper.buildBasicOperator("$exists", value);
    }
}