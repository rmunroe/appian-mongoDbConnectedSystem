package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.futures;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
//import org.apache.log4j.Logger;


@MongoDbCategory
public class M_ToJson {


    @Function
    public String m_ToJson(TypeService typeService, ContentService contentService, @Parameter TypedValue value) throws Exception {
//        LOG.debug("m_ToJson was called");

        try {
            return MongoDbJsonHelper.typedValueToDocument(typeService, value).toJson();
        } catch (Exception e) {
            throw new Exception("The value passed in must be a Dictionary or CDT");
        }
    }
}