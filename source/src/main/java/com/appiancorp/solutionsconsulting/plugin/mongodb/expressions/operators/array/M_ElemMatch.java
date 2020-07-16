package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.array;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

import java.util.Arrays;

@MongoDbCategory
public class M_ElemMatch {
    @Function
    public String m_ElemMatch(@Parameter String... queryExpressions) {
        return MongoDbJsonHelper.buildArrayOperator("$elemMatch", Arrays.asList(queryExpressions), false);
    }
}