package com.hixiu.framework.datasource;

import java.lang.annotation.*;

/**
 * @创建人:程建乐
 * @创建时间: 2019/9/1
 * @描述
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSourceAutowired {

    String value();

}
