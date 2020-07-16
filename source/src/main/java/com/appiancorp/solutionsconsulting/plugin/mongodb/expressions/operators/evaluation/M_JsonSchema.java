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
public class M_JsonSchema {
    @Function
    public String m_JsonSchema(TypeService typeService, @Parameter TypedValue jsonSchema) throws JAXBException, ParseException {
        String schemaString = MongoDbJsonHelper.getJsonValueFromDictOrString(typeService, jsonSchema);
        if (!schemaString.startsWith("{")) schemaString = "{ " + schemaString + " }";

        return MongoDbJsonHelper.buildBasicOperator("$jsonSchema", schemaString, true);
    }
}