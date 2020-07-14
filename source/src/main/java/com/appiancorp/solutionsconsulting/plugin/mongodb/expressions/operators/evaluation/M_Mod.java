package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.evaluation;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_Mod {
    @Function
    public String m_Mod(@Parameter Integer divisor, @Parameter Integer remainder) {
        return "\"$mod\": [ " + divisor + ", " + remainder + " ]";
    }
}