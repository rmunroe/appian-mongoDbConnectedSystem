package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.core.data.DateWithTimezone;
import com.appiancorp.core.data.TimestampWithTimezone;
import com.appiancorp.ps.plugins.typetransformer.AppianElement;
import com.appiancorp.ps.plugins.typetransformer.AppianList;
import com.appiancorp.ps.plugins.typetransformer.AppianObject;
import com.appiancorp.ps.plugins.typetransformer.AppianTypeFactory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Binary;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.AppianType;
import com.appiancorp.suiteapi.type.Datatype;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@MongoDbCategory
public class MdbToJson {
    private static final Logger LOG = Logger.getLogger(MdbToJson.class);

    @Function
    public String mdbToJson(TypeService typeService, ContentService contentService, @Parameter TypedValue value) throws Exception {
        LOG.debug("mdbToJson was called");

        Document doc = typedValueToDocument(typeService, value);

        return doc.toJson();
    }

    private static Document typedValueToDocument(TypeService typeService, TypedValue typedValue) throws JAXBException, ParseException {
        AppianTypeFactory typeFactory = AppianTypeFactory.newInstance(typeService);

        Document doc = new Document();

        Datatype binaryType = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MCSH", "Binary"));
        Datatype objectIdType = typeService.getTypeByQualifiedName(new QName("urn:com:appian:types:MCSH", "ObjectId"));

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
                try {
                    // If we can cast it to a Dictionary, recurse
                    typeService.cast(AppianTypeLong.DICTIONARY, dictionary.get(key));
                    doc.put(keyName, typedValueToDocument(typeService, dictionary.get(key)));

                } catch (Exception e) {
                    // Otherwise it's a primitive
                    Object value = dictionary.get(key).getValue();

                    if (value instanceof Timestamp) {
                        // Appian uses Timestamp for Date Time
                        doc.put(keyName, new Date(((Timestamp) value).getTime()));

                    } else if (value instanceof TimestampWithTimezone) {
                        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                        Date parsed = format.parse(((TimestampWithTimezone) value).toString());
                        doc.put(keyName, parsed);

                    } else if (value instanceof DateWithTimezone) {
                        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
                        Date parsed = format.parse(((DateWithTimezone) value).toString());
                        doc.put(keyName, parsed);

                    } else if (value instanceof java.sql.Time) {
                        // There is no good way to handle Time only
                        doc.put(keyName, value.toString());

                    } else {
                        doc.put(keyName, value);
                    }
                }
            }
        }

        return doc;
    }
}