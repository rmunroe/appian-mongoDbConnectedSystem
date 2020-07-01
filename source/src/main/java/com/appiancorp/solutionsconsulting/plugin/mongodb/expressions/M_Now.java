package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.expression.annotations.Function;

import java.sql.Timestamp;
import java.util.Date;

@MongoDbCategory
public class M_Now {

    @Function
    public Timestamp m_Now() {
        return new Timestamp((new Date()).getTime());
    }
}
