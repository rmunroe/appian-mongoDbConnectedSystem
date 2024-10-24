package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_FindByObjectId {
    @Function
    public String m_FindByObjectId(@Parameter String hexadecimal) {
        return "{ \"_id\": ObjectId(\"" + hexadecimal + "\") }";
    }
}
