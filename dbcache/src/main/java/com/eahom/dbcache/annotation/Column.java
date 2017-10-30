package com.eahom.dbcache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by eahom on 17/5/26.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";
    boolean nameAutoUpperCase() default true;
    String type() default "";
    boolean primaryKey() default false;
    boolean notNull() default false;
    String dateFormatIfDate() default FieldDateFormat.yyyy_MM_DD_HH_mm_ss;
    boolean autoIncrement() default false;
    boolean index() default false;
}
