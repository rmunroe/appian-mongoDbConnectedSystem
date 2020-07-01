package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.operators.comparison;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;

import java.util.ArrayList;
import java.util.List;

@MongoDbCategory
public class Mdb_Nin {
    @Function
    public String mdb_Nin(TypeService typeService, @Parameter TypedValue array) {
        List<String> jsonValues = new ArrayList<>();

        // Force it to be an array
        TypedValue listOfVariant = typeService.cast(AppianTypeLong.LIST_OF_VARIANT, array);

        for (TypedValue typedValue : (TypedValue[]) listOfVariant.getValue()) {
            jsonValues.add(MongoDbJsonHelper.getJsonValue(typedValue.getValue()));
        }

        return "{ $nin: [" + String.join(", ", jsonValues) + "] }";
    }
}