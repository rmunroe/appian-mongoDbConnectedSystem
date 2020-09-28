
package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import org.bson.BsonDocument;
import org.bson.json.JsonWriterSettings;

@MongoDbCategory
public class M_FormatJson {

    @Function
    public String m_FormatJson(String mongoDbJson) {
        BsonDocument bsonDocument = BsonDocument.parse(mongoDbJson);
        JsonWriterSettings.Builder settingsBuilder = JsonWriterSettings.builder().indent(true);
        return bsonDocument.toJson(settingsBuilder.build());
    }
}
