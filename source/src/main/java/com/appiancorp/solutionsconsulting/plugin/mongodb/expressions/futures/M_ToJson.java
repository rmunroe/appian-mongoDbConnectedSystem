package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.futures;

import com.appiancorp.core.data.DateWithTimezone;
import com.appiancorp.core.data.TimestampWithTimezone;
import com.appiancorp.ps.plugins.typetransformer.AppianTypeFactory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.AppianTypeHelper;
import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.Binary;
import com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes.ObjectId;
import com.appiancorp.solutionsconsulting.plugin.mongodb.expressions.MongoDbJsonHelper;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;
import com.appiancorp.suiteapi.type.Datatype;
import com.appiancorp.suiteapi.type.TypeService;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;
//import org.apache.log4j.Logger;
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
public class M_ToJson {
//    private static final Logger LOG = Logger.getLogger(M_ToJson.class);


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