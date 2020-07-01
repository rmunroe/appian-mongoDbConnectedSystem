package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.comparison;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypedValue;

@MongoDbCategory
public class M_Ne {
    @Function
    public String m_Ne(@Parameter TypedValue value) {
        return MongoDbJsonHelper.buildBasicOperator("$ne", value);
    }
}