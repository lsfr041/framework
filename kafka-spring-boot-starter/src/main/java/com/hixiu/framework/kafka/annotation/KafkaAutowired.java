package com.hixiu.framework.kafka.annotation;

import java.lang.annotation.*;

/**
 * @ClassName KafkaAutowired
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 15:08
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KafkaAutowired {
    String value() default "middleware.kafka";
}
