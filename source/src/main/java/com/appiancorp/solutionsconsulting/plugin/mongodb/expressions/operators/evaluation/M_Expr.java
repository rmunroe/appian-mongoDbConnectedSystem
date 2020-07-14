package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.evaluation;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Expr {
    @Function
    public String m_Expr(@Parameter String operatorExpression) {
        return "\"$expr\": { " + operatorExpression + " }";
    }
}