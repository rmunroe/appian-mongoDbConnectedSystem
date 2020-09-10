package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import org.bson.Document;

@MongoDbCategory
public class M_ValidObjectId {
    @Function
    public Boolean m_ValidObjectId(@Parameter String hexadecimal) {
        try {
            Document.parse("{_id: ObjectId(\"" + hexadecimal + "\")}");
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
}
