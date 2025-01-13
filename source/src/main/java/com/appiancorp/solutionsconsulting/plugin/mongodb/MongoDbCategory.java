package com.appiancorp.solutionsconsulting.plugin.mongodb;

import com.appiancorp.suiteapi.expression.annotations.Category;

import java.lang.annotation.*;

/**
 * Annotation that categorizes methods or types under the MongoDb Category
 * for Appian expressions.
 *
 * <p>Applying this annotation to methods or classes allows Appian to group them
 * under a MongoDB-focused category, potentially making them easier to
 * identify or organize in an Appian environment.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
@Category("MongoDbCategory")
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MongoDbCategory {

}
