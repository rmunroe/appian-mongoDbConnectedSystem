package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.comparison;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;

@MongoDbCategory
public class M_In {
    @Function
    public String m_In(TypeService typeService, @Parameter TypedValue... array) {
        return MongoDbJsonHelper.buildArrayOperator("$in", MongoDbJsonHelper.getJsonValuesFromArray(typeService, array), false);
    }
}