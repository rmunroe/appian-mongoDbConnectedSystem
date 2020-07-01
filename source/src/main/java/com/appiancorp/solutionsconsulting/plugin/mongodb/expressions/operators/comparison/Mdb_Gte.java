package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.comparison;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypedValue;

@MongoDbCategory
public class Mdb_Gte {
    @Function
    public String mdb_Gte(@Parameter TypedValue value) {
        return MongoDbJsonHelper.buildBasicOperator("$gte", value);
    }
}