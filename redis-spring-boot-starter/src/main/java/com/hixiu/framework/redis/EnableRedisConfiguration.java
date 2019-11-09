/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: EnableRedisConfiguration
 * Author:   程建乐
 * Date:     2019/9/1 0:46
 * Description: EnableRedisConfiguration
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈EnableRedisConfiguration〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RedisConfigurationRegistrar.class)
public @interface EnableRedisConfiguration {

    /**
     * 存储了redis-starter全套配置的apollo namespace名称
     *
     * @return
     */
    String[] value();

    /**
     * 是否作为primary bean注入ioc
     *
     * @return
     * @see org.springframework.context.annotation.Primary
     */
    boolean primary() default false;
}
