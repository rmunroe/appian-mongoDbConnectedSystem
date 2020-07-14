package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.evaluation;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Regex {
    @Function
    public String m_Regex(@Parameter String regex, @Parameter String options) {
        return "\"$regex\": /" + regex + "/" + options;
    }
}