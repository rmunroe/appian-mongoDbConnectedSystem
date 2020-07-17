package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.geospatial;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Point;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.Type;

@MongoDbCategory
public class M_GeoWithinCircle {
    @Function
    public String m_GeoWithinCircle(
            @Parameter @Type(namespace = Point.NAMESPACE_URI, name = Point.LOCAL_PART) Point centerPoint,
            @Parameter Double radius
    ) {
        return MongoDbJsonHelper.buildBasicOperator(
                "$geoWithin",
                "{ " +
                        MongoDbJsonHelper.buildBasicOperator(
                                "$center",
                                "[ "
                                        + centerPoint.toString() + ","
                                        + radius
                                        + " ]",
                                true
                        ) + " }",
                true
        );
    }
}