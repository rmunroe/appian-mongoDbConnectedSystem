package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.comparison;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.AppianType;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;

import javax.xml.bind.JAXBException;
import java.text.ParseException;

@MongoDbCategory
public class M_Eq {
    @Function
    public String m_Eq(TypeService typeService, @Parameter TypedValue value) throws JAXBException, ParseException {
        if (value.getInstanceType() == AppianType.STRING && value.getValue().toString().startsWith("{ \"$oid\": \""))
            return MongoDbJsonHelper.buildBasicOperator(typeService, "$eq", value, true);
        else
            return MongoDbJsonHelper.buildBasicOperator(typeService, "$eq", value);
    }
}