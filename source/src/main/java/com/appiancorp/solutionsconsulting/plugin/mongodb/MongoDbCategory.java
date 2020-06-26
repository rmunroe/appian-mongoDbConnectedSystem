package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appiancorp.suiteapi.expression.annotations.Category;

import java.lang.annotation.*;

@Category("MongoDbCategory")
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MongoDbCategory {

}
