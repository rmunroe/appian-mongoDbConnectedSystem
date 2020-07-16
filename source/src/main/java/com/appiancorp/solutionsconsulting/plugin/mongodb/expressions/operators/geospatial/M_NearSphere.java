package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.geospatial;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;

import javax.xml.bind.JAXBException;
import java.text.ParseException;

@MongoDbCategory
public class M_NearSphere {
    @Function
    public String m_NearSphere(TypeService typeService, @Parameter TypedValue geoJson, @Parameter Double minDistance, @Parameter Double maxDistance) throws JAXBException, ParseException {
        String geoJsonString = MongoDbJsonHelper.getJsonValueFromDictOrString(typeService, geoJson);

        return MongoDbJsonHelper.buildBasicOperator(
                "$nearSphere",
                "{ "
                        + MongoDbJsonHelper.buildBasicOperator("$geometry", geoJsonString, true)
                        + ", "
                        + MongoDbJsonHelper.buildBasicOperator("$minDistance", minDistance)
                        + ", "
                        + MongoDbJsonHelper.buildBasicOperator("$maxDistance", maxDistance)
                        + " }",
                true
        );
    }
}