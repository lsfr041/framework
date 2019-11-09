/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisConfigurationRegistrar
 * Author:   程建乐
 * Date:     2019/9/1 0:50
 * Description: RedisConfigurationRegistrar
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import com.hixiu.framework.common.inject.AbstractConfigurationRegistrar;
import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisConfigurationRegistrar〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisConfigurationRegistrar extends AbstractConfigurationRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AutowiredAnnotationConfig[] configs = parseAutowiredAnnotationConfigs(metadata, EnableRedisConfiguration.class.getName());
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        Object processor = new RedisAutowiredAnnotationProcessor(beanFactory, configs);
        beanFactory.registerSingleton(RedisAutowiredAnnotationProcessor.CLASS_NAME, processor);
        RedisLockAspect redisLockAspect = new RedisLockAspect(beanFactory);
        beanFactory.registerSingleton(RedisLockAspect.CLASS_NAME, redisLockAspect);
    }
}
