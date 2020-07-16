package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appiancorp.ps.plugins.typetransformer.AppianElement;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidCdtException;
import com.appiancorp.solutionsconsulting.plugin.mongodb.exceptions.InvalidDictionaryException;
import com.appiancorp.suiteapi.type.Datatype;
import com.appiancorp.suiteapi.type.DatatypeProperties;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.suiteapi.type.exceptions.InvalidTypeException;
import com.appiancorp.type.AppianTypeLong;
import com.appiancorp.type.DataTypeProperties;

import java.util.*;


public class AppianTypeHelper {

    /**
     * Takes the first object (Dictionary or CDT) in the sourceObjects and returns the field names, useful for
     *
     * @param typeService
     * @param value
     * @return
     */
    public static String[] getFieldNames(TypeService typeService, TypedValue value) throws Exception {
        Map<TypedValue, TypedValue> map = (HashMap<TypedValue, TypedValue>) typeService.cast(AppianTypeLong.LIST_OF_DICTIONARY, value).getValue();

        Set<TypedValue> keySet = map.keySet();
        ArrayList<String> fieldNames = new ArrayList<>(keySet.size());
        for (TypedValue key : keySet) {
            String fieldName = key.getValue().toString();
            fieldName = fieldName.replaceAll("[_]", " ");
            fieldNames.add(fieldName);
        }

        String[] header = fieldNames.toArray(new String[fieldNames.size()]);

        return header;
    }

    /**
     * @param typeService
     * @param typedValue
     * @return
     * @throws InvalidCdtException
     */
    public static Boolean isListDictOrCdt(TypeService typeService, TypedValue typedValue) {
        if (typedValue == null) return false;
        try {
            new ArrayList<>(Arrays.asList((HashMap<TypedValue, TypedValue>[]) typeService.cast(AppianTypeLong.LIST_OF_DICTIONARY, typedValue).getValue()));
        } catch (Exception e1) {
            try {
                typeService.cast(AppianTypeLong.DICTIONARY, typedValue).getValue();
            } catch (Exception e2) {
                return false;
            }
        }
        return true;
    }


    /**
     * @param typeService
     * @param typedValue
     * @return
     * @throws InvalidCdtException
     */
    public static ArrayList<HashMap<TypedValue, TypedValue>> toMapList(TypeService typeService, TypedValue typedValue) throws InvalidCdtException {
        try {
            return new ArrayList<>(Arrays.asList((HashMap<TypedValue, TypedValue>[]) typeService.cast(AppianTypeLong.LIST_OF_DICTIONARY, typedValue).getValue()));
        } catch (Exception e1) {
            try {
                HashMap<TypedValue, TypedValue> returnMap = (HashMap<TypedValue, TypedValue>) typeService.cast(AppianTypeLong.DICTIONARY, typedValue).getValue();
                ArrayList<HashMap<TypedValue, TypedValue>> returnList = new ArrayList<HashMap<TypedValue, TypedValue>>();
                returnList.add(returnMap);
                return (ArrayList<HashMap<TypedValue, TypedValue>>) returnList.clone();
            } catch (Exception e2) {
                throw new InvalidCdtException("Invalid CDT");
            }
        }

    }


    /**
     * @param ts
     * @param toCast
     * @return
     * @throws InvalidTypeException
     */
    public static TypedValue toDictionaryList(TypeService ts, ArrayList<HashMap<TypedValue, TypedValue>> toCast) throws InvalidTypeException {
        try {
            return new TypedValue(AppianTypeLong.LIST_OF_DICTIONARY, toCast.toArray(new HashMap[toCast.size()]));
        } catch (Exception e) {
            throw new InvalidTypeException("Could not cast to list of dictionary");
        }
    }


    /**
     * @param fieldsAndValues
     * @return
     * @throws InvalidDictionaryException
     */
    public static HashMap<TypedValue, TypedValue> toHashMap(TypedValue fieldsAndValues) throws InvalidDictionaryException {
        HashMap<TypedValue, TypedValue> returnMap = new HashMap<TypedValue, TypedValue>();
        try {
            returnMap.putAll((HashMap<TypedValue, TypedValue>) fieldsAndValues.getValue());
            return (HashMap<TypedValue, TypedValue>) returnMap.clone();
        } catch (Exception e) {
            throw new InvalidDictionaryException("Invalid dictionary");
        }
    }


    /**
     * Converts a Set of TypedValue to an ArrayList of TypedValue
     *
     * @param set
     * @return
     */
    public static ArrayList<TypedValue> setToArrayList(Set<TypedValue> set) {
        return new ArrayList<TypedValue>(Arrays.asList(set.toArray(new TypedValue[set.size()])));
    }


    /**
     * Attempts to get the scalar type of the given type. Will throw an error if passed a CDT or list of variant (2d array)
     *
     * @param typeService
     * @param typedValue
     * @return
     * @throws InvalidTypeException
     * @throws InvalidDictionaryException
     */
    public static TypedValue getScalarType(TypeService typeService, TypedValue typedValue) throws InvalidTypeException, InvalidDictionaryException {
        Long typeLong = typedValue.getInstanceType();
        DatatypeProperties typeProperties = typeService.getDatatypeProperties(typeLong);
        List<Datatype> referencedTypes = typeService.getReferencedTypes(typeLong);

        if (!typeProperties.hasFlag(DataTypeProperties.FLAG_SYSTEM) || typeLong == AppianTypeLong.LIST_OF_VARIANT)
            throw new InvalidDictionaryException("Nested CDTs and lists currently not supported.");

        Long type = 0L;

        if (!typeProperties.isListType())
            type = typeLong;
        else {
            // Loops through all the referenced types and checks for the one that's list type equals the given type
            int i;
            for (i = 0; i < referencedTypes.size(); i++) {
                Long listType = referencedTypes.get(i).getList();
                if (listType != null) {
                    if (listType.equals(typeLong)) {
                        type = referencedTypes.get(i).getTypeof();
                        break;
                    }
                }
            }
        }

        return new TypedValue(type);
    }


    /**
     * Attempts to cast an Object to an Object[]
     *
     * @param obj
     * @return
     */
    public static Object[] toObjectArr(Object obj) {
        Object[] objArr;
        try {
            objArr = (Object[]) obj;
        } catch (Exception e) {
            objArr = new Object[1];
            objArr[0] = obj;
        }
        return objArr;
    }
}
