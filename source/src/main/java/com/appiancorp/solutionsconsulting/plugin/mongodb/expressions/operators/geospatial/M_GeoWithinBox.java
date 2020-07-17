package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.geospatial;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Point;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.Type;

@MongoDbCategory
public class M_GeoWithinBox {
    @Function
    public String m_GeoWithinBox(
            @Parameter Point bottomLeft,
            @Parameter Point topRight
    ) {
        return MongoDbJsonHelper.buildBasicOperator(
                "$geoWithin",
                "{ " +
                        MongoDbJsonHelper.buildBasicOperator(
                                "$box",
                                new Point[]{bottomLeft, topRight},
                                true
                        ) + " }",
                true
        );
    }
}