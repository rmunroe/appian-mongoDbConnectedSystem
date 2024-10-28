package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

@MongoDbCategory
public class M_ObjectId {
    @Function
    public String m_ObjectId(@Parameter String hexadecimal) {
        return "{ \"$oid\": \"" + hexadecimal + "\" }";
    }
}
