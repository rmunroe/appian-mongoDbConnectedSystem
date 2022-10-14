package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import org.bson.BsonDocument;
import org.bson.json.JsonWriterSettings;

@MongoDbCategory
public class M_FormatJson {

    @Function
    public String m_FormatJson(@Parameter String mongoDbJson) {
        BsonDocument bsonDocument = BsonDocument.parse(mongoDbJson);
        JsonWriterSettings.Builder settingsBuilder = JsonWriterSettings.builder().indent(true);
        return bsonDocument.toJson(settingsBuilder.build());
    }
}
