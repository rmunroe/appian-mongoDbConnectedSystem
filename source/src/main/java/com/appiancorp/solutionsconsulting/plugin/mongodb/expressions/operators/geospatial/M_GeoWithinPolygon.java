package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.geospatial;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Point;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_GeoWithinPolygon {
    @Function
    public String m_GeoWithinPolygon(
            @Parameter Point[] polygonPoints
    ) {
        return MongoDbJsonHelper.buildBasicOperator(
                "$geoWithin",
                "{ " +
                        MongoDbJsonHelper.buildBasicOperator(
                                "$polygon",
                                MongoDbJsonHelper.geoPointArrayToString(polygonPoints),
                                true
                        ) + " }",
                true
        );
    }
}