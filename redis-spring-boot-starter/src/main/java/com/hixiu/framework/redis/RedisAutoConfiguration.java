/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisAutoConfiguration
 * Author:   程建乐
 * Date:     2019/9/1 0:47
 * Description: RedisAutoConfiguration
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisAutoConfiguration〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class RedisAutoConfiguration {

    @Bean(name = RedisAutowiredAnnotationProcessor.CLASS_NAME)
    @ConditionalOnMissingBean(name = RedisAutowiredAnnotationProcessor.CLASS_NAME)
    public RedisAutowiredAnnotationProcessor redisAutowiredAnnotationBeanPostProcessor() {
        return new RedisAutowiredAnnotationProcessor();
    }

    @Bean(name = RedisLockAspect.CLASS_NAME)
    @ConditionalOnMissingBean(name = RedisLockAspect.CLASS_NAME)
    public RedisLockAspect redisLockAspect() {
        return new RedisLockAspect();
    }
}
