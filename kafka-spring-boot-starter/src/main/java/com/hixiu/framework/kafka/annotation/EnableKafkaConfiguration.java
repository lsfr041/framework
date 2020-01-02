package com.hixiu.framework.kafka.annotation;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(KafkaConfigurationRegistrar.class)
public @interface EnableKafkaConfiguration {
    /**
     * 存储了kafka-starter全套配置的apollo namespace名称
     *
     * @return
     */
    String[] value() default "middleware.kafka";

    /**
     * 是否作为primary bean注入ioc
     *
     * @return
     * @see org.springframework.context.annotation.Primary
     */
    boolean primary() default false;
}
