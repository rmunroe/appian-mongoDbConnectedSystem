package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.geospatial;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Point;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.Type;

@MongoDbCategory
public class M_ToPoint {
    @Function
    public @Type(namespace = Point.NAMESPACE_URI, name = Point.LOCAL_PART)
    Point
    m_ToPoint(@Parameter Double longitude, @Parameter Double latitude) {
        return new Point(longitude, latitude);
    }
}