package com.company.permit.framework.dataperm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    String alias() default "";

    String deptColumn() default "dept_id";

    String userColumn() default "create_by";
}
