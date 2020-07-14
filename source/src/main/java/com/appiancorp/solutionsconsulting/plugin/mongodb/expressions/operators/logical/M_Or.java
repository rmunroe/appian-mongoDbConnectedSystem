package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.logical;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

import java.util.Arrays;

@MongoDbCategory
public class M_Or {
    @Function
    public String m_Or(@Parameter String... queryExpressions) {
        return MongoDbJsonHelper.buildArrayOperator("$or", Arrays.asList(queryExpressions), true);
    }
}