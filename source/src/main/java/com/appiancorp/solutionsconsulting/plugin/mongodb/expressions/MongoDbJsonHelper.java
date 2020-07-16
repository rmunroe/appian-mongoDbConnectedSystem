package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.core.data.DateWithTimezone;
import com.appiancorp.core.data.TimestampWithTimezone;
import com.appiancorp.ps.plugins.typetransformer.AppianTypeFactory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.AppianTypeHelper;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Binary;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId;
import com.appiancorp.suiteapi.type.Datatype;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MongoDbJsonHelper {

    public static Document typedValueToDocument(TypeService typeService, TypedValue typedValue) throws JAXBException, ParseException {
        AppianTypeFactory typeFactory = AppianTypeFactory.newInstance(typeService);

        Document doc = new Document();

        Datatype binaryType = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MongoDB", "Binary"));
        Datatype objectIdType = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MongoDB", "ObjectId"));

        @SuppressWarnings("unchecked")
        Map<TypedValue, TypedValue> dictionary = (HashMap<TypedValue, TypedValue>) typeService.cast(AppianTypeLong.DICTIONARY, typedValue).getValue();
        for (TypedValue key : dictionary.keySet()) {
            String keyName = key.getValue().toString();

            if (dictionary.get(key).getInstanceType().equals(binaryType.getId())) {
                // Is Binary
                Binary ourBin = typeFactory.toJavaObject(dictionary.get(key), Binary.class);
                org.bson.types.Binary value = new org.bson.types.Binary(
                        (byte) ((byte) 48 - ourBin.getType().getBytes()[0]),
                        Base64.getDecoder().decode(ourBin.getBinary().getBytes())
                );
                doc.put(keyName, value);

            } else if (dictionary.get(key).getInstanceType().equals(objectIdType.getId())) {
                // Is ObjectID
                ObjectId ourOid = typeFactory.toJavaObject(dictionary.get(key), ObjectId.class);
                org.bson.types.ObjectId value = new org.bson.types.ObjectId(ourOid.getOid());
                doc.put(keyName, value);

            } else {
                Object value = dictionary.get(key).getValue();

                if (value instanceof Timestamp) {
                    // Appian uses Timestamp for Date Time
                    doc.put(keyName, new Date(((Timestamp) value).getTime()));

                } else if (value instanceof TimestampWithTimezone) {
                    // Sometimes now() returns this datatype. Unfortunately there is no way to get
                    // more accurate than to the minute.
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm a Z");
                    Date parsed = format.parse(value.toString());
                    doc.put(keyName, parsed);

                } else if (value instanceof DateWithTimezone) {
                    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                    Date parsed = format.parse(value.toString());
                    doc.put(keyName, parsed);

                } else if (value instanceof java.sql.Time) {
                    // There is no good way to handle Time only
                    doc.put(keyName, value.toString());

                } else if (value instanceof Object[]) {
                    // Handle arrays... BSON prefers List<>
                    doc.put(keyName, Arrays.asList((Object[]) value));

                } else {
                    // Was not a datatype with special handling
                    try {
                        // If we can cast it to a Dictionary, recurse
                        typeService.cast(AppianTypeLong.DICTIONARY, dictionary.get(key));
                        doc.put(keyName, typedValueToDocument(typeService, dictionary.get(key)));

                    } catch (Exception e) {
                        // When all else fails, just put
                        doc.put(keyName, value);
                    }
                }
            }
        }

        return doc;
    }

    /**
     * Handler for accepting either a Dictionary (which will be toJson()'d) or a JSON string
     *
     * @param typeService
     * @param typedValue
     * @return
     * @throws JAXBException
     * @throws ParseException
     */
    public static String getJsonValueFromDictOrString(TypeService typeService, TypedValue typedValue) throws JAXBException, ParseException {
        if (AppianTypeHelper.isListDictOrCdt(typeService, typedValue)) {
            return MongoDbJsonHelper.typedValueToDocument(typeService, typedValue).toJson();
        } else {
            return MongoDbJsonHelper.getJsonValueFromTypedValue(typeService, typedValue, true);
        }
    }


    /**
     * Returns the value ready for insertion as the value of a JSON filter
     *
     * @param valObject The value of the underlying TypedValue (e.g. myTypedValue.getValue())
     * @return value ready for insertion as the value of a JSON filter
     */
    public static String getJsonValueFromObject(Object valObject) {
        return getJsonValueFromObject(valObject, false);
    }


    public static String getJsonValueFromObject(Object valObject, Boolean noQuotes) {
        String valString;

        if (valObject == null || (valObject instanceof String && StringUtils.isEmpty(valObject.toString()))) {
            valString = "null";

        } else if (valObject instanceof Integer || valObject instanceof Long || valObject instanceof Double) {
            valString = valObject.toString();

        } else if (valObject instanceof Boolean) {
            valString = valObject.toString();

        } else if (valObject instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            String text = sdf.format(valObject);
            valString = "ISODate(\"" + text + "\")";

        } else {
            // Strings will match here
            if (noQuotes)
                valString = valObject.toString();
            else
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
        return getJsonValueFromTypedValue(typeService, value, false);
    }

    public static String getJsonValueFromTypedValue(TypeService typeService, TypedValue value, Boolean noQuotes) {
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
        return getJsonValueFromObject(value.getValue(), noQuotes);
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

    public static String buildBasicOperator(String operator, Object value, Boolean noQuotes) {
        return "\"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromObject(value, noQuotes);
    }

    public static String buildBasicOperator(String operator, Object value) {
        return "\"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromObject(value);
    }


    public static String buildBasicOperator(TypeService typeService, String operator, TypedValue value) throws JAXBException, ParseException {
        return buildBasicOperator(typeService, operator, value, false);
    }

    public static String buildBasicOperator(TypeService typeService, String operator, TypedValue value, Boolean noQuotes) throws JAXBException, ParseException {
        if (AppianTypeHelper.isListDictOrCdt(typeService, value))
            return "\"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromDictOrString(typeService, value);
        else
            return "\"" + operator + "\": " + MongoDbJsonHelper.getJsonValueFromObject(value, noQuotes);
    }


    public static String buildArrayOperator(String operator, List<String> jsonValues, Boolean encloseEachInBraces) {
        if (encloseEachInBraces)
            for (int i = 0; i < jsonValues.size(); i++) {
                if (!jsonValues.get(i).matches("/^\\{.*}$"))
                    jsonValues.set(i, "{ " + jsonValues.get(i) + " }");
            }

        return "\"" + operator + "\": [ " + String.join(", ", jsonValues) + " ]";
    }
}
