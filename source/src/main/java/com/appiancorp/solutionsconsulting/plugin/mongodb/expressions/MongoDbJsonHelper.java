package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.suiteapi.type.TypedValue;

import java.util.Date;

public class MongoDbJsonHelper {
    /**
     * Returns the value ready for insertion as the value of a JSON filter
     *
     * @param valObject The value of the underlying TypedValue (e.g. myTypedValue.getValue())
     * @return value ready for insertion as the value of a JSON filter
     */
    public static String getJsonValue(Object valObject) {
        String valString;

        if (valObject instanceof String) {
            valString = "\"" + valObject.toString() + "\"";

        } else if (valObject instanceof Integer || valObject instanceof Long || valObject instanceof Double) {
            valString = valObject.toString();

        } else if (valObject instanceof Date) {
            valString = "{ $date: " + ((Date) valObject).getTime() + " }";

        } else {
            valString = "\"" + valObject.toString() + "\"";
        }

        return valString;
    }

    public static String buildBasicOperator(String operator, TypedValue value) {
        return "{ " + operator + ": " + MongoDbJsonHelper.getJsonValue(value.getValue()) + " }";
    }
}
