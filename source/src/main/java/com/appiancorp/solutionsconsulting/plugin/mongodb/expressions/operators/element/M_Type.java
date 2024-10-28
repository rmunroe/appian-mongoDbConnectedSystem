package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.element;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;

import javax.xml.bind.JAXBException;
import java.text.ParseException;

@MongoDbCategory
public class M_Type {
    @Function
    public String m_Type(TypeService typeService, @Parameter TypedValue... types) throws JAXBException, ParseException {
        if (types.length == 1) {
            if (typeService.getDatatypeProperties(types[0].getInstanceType()).getName().matches("^List of .*")) {
                return MongoDbJsonHelper.buildArrayOperator("$type", MongoDbJsonHelper.getJsonValuesFromArray(typeService, types), false);
            } else {
                return MongoDbJsonHelper.buildBasicOperator(typeService, "$type", types[0]);
            }
        } else {
            return MongoDbJsonHelper.buildArrayOperator("$type", MongoDbJsonHelper.getJsonValuesFromArray(typeService, types), false);
        }
    }
}