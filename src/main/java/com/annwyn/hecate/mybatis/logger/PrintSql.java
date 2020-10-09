package com.annwyn.hecate.mybatis.logger;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrintSql {

    boolean value() default true;

}
