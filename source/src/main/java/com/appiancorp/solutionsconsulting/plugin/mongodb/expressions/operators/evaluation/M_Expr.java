package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.evaluation;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;

import javax.xml.bind.JAXBException;
import java.text.ParseException;

@MongoDbCategory
public class M_Expr {
    @Function
    public String m_Expr(TypeService typeService, @Parameter TypedValue operatorExpression) throws JAXBException, ParseException {
        String expressionString = MongoDbJsonHelper.getJsonValueFromDictOrString(typeService, operatorExpression);
        if (!expressionString.startsWith("{")) expressionString = "{ " + expressionString + " }";

        return MongoDbJsonHelper.buildBasicOperator("$expr", expressionString, true);
    }
}