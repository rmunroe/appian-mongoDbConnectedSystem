package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDbJsonHelper {
    /**
     * Returns the value ready for insertion as the value of a JSON filter
     *
     * @param valObject The value of the underlying TypedValue (e.g. myTypedValue.getValue())
     * @return value ready for insertion as the value of a JSON filter
     */
    public static String getJsonValueFromObject(Object valObject) {
        String valString;

        if (valObject == null || (valObject instanceof String && StringUtils.isEmpty(valObject.toString()))) {
            valString = "null";

        } else if (valObject instanceof String) {
            valString = "\"" + valObject.toString() + "\"";

        } else if (valObject instanceof Integer || valObject instanceof Long || valObject instanceof Double) {
            valString = valObject.toString();

        } else if (valObject instanceof Boolean) {
            valString = valObject.toString();

        } else if (valObject instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            String text = sdf.format(valObject);
            valString = "ISODate(\"" + text + "\")";

        } else {
            valString = "\"" + valObject.toString() + "\"";
        }

        return valString;
    }


    /**
     * Returns the value ready for insertion as the value of a JSON filter
     *
     * @param value The TypedValue
     * @return value ready for insertion as the value of a JSON filter
     */
    public static String getJsonValueFromTypedValue(TypeService typeService, TypedValue value) {
        if (typeService.getDatatypeProperties(value.getInstanceType()).getName().equals("Boolean")) {
            // Special case for handling booleans. Appian returns them as a Long, 1 if true.
            return value.getValue().equals(1L) ? getJsonValueFromObject(true) : getJsonValueFromObject(false);

        } else if (value.getValue() instanceof String) {
            // Special handling for string values
            if (((String) value.getValue()).matches("^ObjectId\\(\".*\"\\)$")) {
                return value.getValue().toString();
            }
        }

        // No special case matched
        return getJsonValueFromObject(value.getValue());
    }


    public static List<String> getJsonValuesFromArray(TypeService typeService, TypedValue[] array) {
        List<String> jsonValues = new ArrayList<>();

        if (array.length == 1 &&
                typeService.getDatatypeProperties(array[0].getInstanceType()).getName().matches("^List of .*")) {
            for (Object object : (Object[]) array[0].getValue())
                jsonValues.add(getJsonValueFromObject(object));
        } else {
            for (TypedValue typedValue : array)
                jsonValues.add(getJsonValueFromObject(typedValue.getValue()));
        }

        return jsonValues;
    }


    public static String buildBasicOperator(TypeService typeService, String operator, TypedValue value) {
        return "\"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromTypedValue(typeService, value);
    }


    public static String buildArrayOperator(String operator, List<String> jsonValues, Boolean encloseInBraces) {
        if (encloseInBraces)
            for (int i = 0; i < jsonValues.size(); i++) {
                if (!jsonValues.get(i).matches("/^\\{.*}$"))
                    jsonValues.set(i, "{ " + jsonValues.get(i) + " }");
            }

        return "\"" + operator + "\": [ " + String.join(", ", jsonValues) + " ]";
    }
}
