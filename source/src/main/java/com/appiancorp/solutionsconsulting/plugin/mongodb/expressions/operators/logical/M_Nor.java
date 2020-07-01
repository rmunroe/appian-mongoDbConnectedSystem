package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.logical;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

import java.util.Arrays;

@MongoDbCategory
public class M_Nor {
    @Function
    public String m_Nor(@Parameter String... queryClauses) {
        return MongoDbJsonHelper.buildArrayOperator("$nor", Arrays.asList(queryClauses), true);
    }
}