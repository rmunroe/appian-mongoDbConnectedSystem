package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.ps.plugins.typetransformer.AppianElement;
import com.appiancorp.ps.plugins.typetransformer.AppianList;
import com.appiancorp.ps.plugins.typetransformer.AppianObject;
import com.appiancorp.ps.plugins.typetransformer.AppianTypeFactory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.AppianType;
import com.appiancorp.suiteapi.type.Datatype;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;


@MongoDbCategory
public class MdbFromJson {
    private static final Logger LOG = Logger.getLogger(MdbFromJson.class);

    /**
     * Copies the values from a BSON Document into an Appian Dictionary
     *
     * @param typeService injected TypeService
     * @param typeFactory our AppianTypeFactory instance
     * @param doc         a BSON Document
     * @return an Appian Dictionary
     */
    private static AppianObject copyFromDocument(TypeService typeService, AppianTypeFactory typeFactory, Document doc) {
        AppianObject dictionary = (AppianObject) typeFactory.createElement(AppianType.DICTIONARY);

        for (String key : doc.keySet()) {
            dictionary.put(key, handleValue(typeService, typeFactory, doc.get(key)));
        }

        return dictionary;
    }

    /**
     * Handles all types noted in https://mongodb.github.io/mongo-java-driver/3.12/bson/documents/#document
     *
     * @param typeService injected TypeService
     * @param typeFactory our AppianTypeFactory instance
     * @param value       any BSON Document value
     * @return an AppianElement
     */
    private static AppianElement handleValue(TypeService typeService, AppianTypeFactory typeFactory, Object value) {
        if (value instanceof Document) {
            // Is a child Document; recurse
            return copyFromDocument(typeService, typeFactory, (Document) value);

        } else if (value instanceof List) {
            List<AppianElement> elements = new ArrayList<>();

            // Empty list?
            if (((ArrayList<?>) value).size() < 1) return null;

            // Recurse for each element
            for (Object val : ((ArrayList<?>) value)) {
                elements.add(handleValue(typeService, typeFactory, val));
            }

            // Create an Appian List of Variant
            AppianList list = typeFactory.createList(AppianType.VARIANT);
            list.addAll(elements);

            return list;

        } else if (value instanceof Date) {
            Timestamp ts = new Timestamp(((Date) value).getTime());
            return typeFactory.createDateTime(ts);

        } else if (value instanceof Boolean) {
            return typeFactory.createBoolean((Boolean) value);

        } else if (value instanceof Double) {
            return typeFactory.createDouble((Double) value);

        } else if (value instanceof Integer) {
            return typeFactory.createLong(Long.valueOf((Integer) value));

        } else if (value instanceof Long) {
            return typeFactory.createLong((Long) value);

        } else if (value instanceof String) {
            return typeFactory.createString((String) value);

        } else if (value instanceof org.bson.types.Binary) {
            // Is a BSON Binary. Convert to our Binary.
            Datatype type = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MCSH", "Binary"));
            AppianObject binary = (AppianObject) typeFactory.createElement(type.getId());
            binary.put("binary", typeFactory.createString(Base64.getEncoder().encodeToString(
                    ((org.bson.types.Binary) value).getData()
            )));
            binary.put("type", typeFactory.createString("" + ((org.bson.types.Binary) value).getType())); // single byte conversion
            return binary;

        } else if (value instanceof org.bson.types.ObjectId) {
            // Is a BSON ObjectId. Convert to our ObjectId.
            Datatype type = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MCSH", "ObjectId"));
            AppianObject oid = (AppianObject) typeFactory.createElement(type.getId());
            oid.put("oid", typeFactory.createString(value.toString()));
            return oid;

        } else if (value == null) {
            return null;
        }

        // Worst case, return the value as a String
        return typeFactory.createString("Unsupported data type: " + value.toString());
    }

    @Function
    public TypedValue mdbFromJson(TypeService typeService, ContentService contentService, @Parameter String json) throws Exception {
        LOG.debug("mdbFromJson was called; create a dictionary and return it");

        AppianTypeFactory typeFactory = AppianTypeFactory.newInstance(typeService);

        Document doc;
        try {
            doc = Document.parse(json);
        } catch (Exception e) {
            LOG.error("Error parsing JSON");
            return null;
        }

        AppianObject dictionary = copyFromDocument(typeService, typeFactory, doc);

        return typeFactory.toTypedValue(dictionary);
    }
}