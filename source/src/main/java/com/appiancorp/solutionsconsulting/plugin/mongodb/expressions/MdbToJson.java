package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.core.data.DateWithTimezone;
import com.appiancorp.core.data.TimestampWithTimezone;
import com.appiancorp.ps.plugins.typetransformer.AppianTypeFactory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Binary;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
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
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@MongoDbCategory
public class MdbToJson {
    private static final Logger LOG = Logger.getLogger(MdbToJson.class);

    /**
     * Takes an Appian
     *
     * @param typeService
     * @param typedValue
     * @return
     * @throws JAXBException
     * @throws ParseException
     */
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

    @Function
    public String mdbToJson(TypeService typeService, ContentService contentService, @Parameter TypedValue value) throws Exception {
        LOG.debug("mdbToJson was called");

        try {
            return typedValueToDocument(typeService, value).toJson();
        } catch (Exception e) {
            throw new Exception("The value passed in must be a Dictionary or CDT");
        }
    }
}