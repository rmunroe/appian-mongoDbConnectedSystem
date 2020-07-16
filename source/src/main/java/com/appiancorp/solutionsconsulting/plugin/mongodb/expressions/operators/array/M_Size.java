package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.array;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Size {
    @Function
    public String m_Size(@Parameter Integer value) {
        return MongoDbJsonHelper.buildBasicOperator("$size", value);
    }
}