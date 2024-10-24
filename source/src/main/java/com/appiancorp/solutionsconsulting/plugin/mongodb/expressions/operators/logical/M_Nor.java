package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.logical;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@MongoDbCategory
public class M_Nor {
    @Function
    public String m_Nor(@Parameter String... queryExpressions) {
        List<String> list = new ArrayList<String>(Arrays.asList(queryExpressions));
        list.removeAll(Arrays.asList("", null));
        return MongoDbJsonHelper.buildArrayOperator("$nor", list, true);
    }
}