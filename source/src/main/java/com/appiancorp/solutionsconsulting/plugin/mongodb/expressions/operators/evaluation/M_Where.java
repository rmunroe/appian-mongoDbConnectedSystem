package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.evaluation;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import org.apache.commons.text.StringEscapeUtils;

@MongoDbCategory
public class M_Where {
    @Function
    public String m_Where(@Parameter String javaScript) {
        return MongoDbJsonHelper.buildBasicOperator("$where", "\"" + StringEscapeUtils.escapeEcmaScript(javaScript) + "\"");
    }
}