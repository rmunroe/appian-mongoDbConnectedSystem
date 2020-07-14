package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.evaluation;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

@MongoDbCategory
public class M_Text {
    @Function
    public String m_Text(@Parameter String search, @Parameter String language, @Parameter Boolean caseSensitive, @Parameter Boolean diacriticSensitive) {
        String retVal = "\"$text\": { \"$search\": \"" + search + "\"";
        if (StringUtils.isNotEmpty(language)) retVal += ", \"$language\": \"" + language + "\"";
        if (caseSensitive != null) retVal += ", \"$caseSensitive\": " + caseSensitive;
        if (diacriticSensitive != null) retVal += ", \"$diacriticSensitive\": " + diacriticSensitive;
        return retVal + " }";
    }
}