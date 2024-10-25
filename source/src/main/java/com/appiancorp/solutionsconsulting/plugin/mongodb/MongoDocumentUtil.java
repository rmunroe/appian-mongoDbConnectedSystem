package com.appiancorp.solutionsconsulting.plugin.mongodb;

import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.Base64;
import java.util.UUID;


public class MongoDocumentUtil {
    private static final String isoDatePattern = "(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])";
    private static final String isoTimePattern = "T(2[0-3]|[01][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[01][0-9]):[0-5][0-9])?";
    private static final String isoDateTimePattern = isoDatePattern + isoTimePattern;
    private static final String appianDatePattern = isoDatePattern + "Z$";


    /**
     * Cleans up any oddities in the Document, such as better handling of ObjectIds and UUIDs
     *
     * @param document A org.bson.Document
     * @return A cleaned HashMap
     */
    public static Document prepDocumentForOutput(Document document, Boolean objectIdAsString, Boolean uuidAsString) {
        for (String key : document.keySet()) {
            Object val = document.get(key);

            if (val instanceof Document) {
                document.put(key, prepDocumentForOutput((Document) val, objectIdAsString, uuidAsString)); // recurse

            } else if (objectIdAsString != null && objectIdAsString && val instanceof ObjectId) {
                Document oid = new Document();
                oid.put("oid", val.toString());
//                com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId oid = new com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId();
//                oid.setOid(val.toString());
                document.put(key, oid);

            } else if (val instanceof Binary) {
                Document binary = new Document();
                binary.put("binary", Base64.getEncoder().encodeToString(
                        ((Binary) val).getData()
                ));
                binary.put("type", "" + ((Binary) val).getType());
                document.put(key, binary);

            } else if (uuidAsString != null && uuidAsString && val instanceof UUID) {
                document.put(key, val.toString());
            }
        }

        return document;
    }


    public static Document prepDocumentForInsert(Document document, Boolean skipDateTimeConversion) {
        for (String key : document.keySet()) {
            Object val = document.get(key);

            if (val instanceof Document) {
                document.put(key, prepDocumentForInsert((Document) val, skipDateTimeConversion)); // recurse
            } else if (!skipDateTimeConversion && val instanceof String && ((String) val).matches(isoDateTimePattern)) {
                // Value matches an ISO date time. Convert it.
                document.put(key, Document.parse("{ \"" + key + "\": ISODate(\"" + val + "\") }").get(key));

            } else if (!skipDateTimeConversion && val instanceof String && ((String) val).matches(appianDatePattern)) {
                // Value matches an ISO date only. Convert it by setting to date with time 0:00 UTC.
                document.put(key, Document.parse("{ \"" + key + "\": ISODate(\"" + ((String) val).replaceFirst("Z$", "") + "T00:00:00.000Z\") }").get(key));
            }
        }

        return document;
    }
}
