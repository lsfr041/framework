package com.hixiu.framework.kafka.annotation;

import com.hixiu.framework.common.inject.AbstractConfigurationRegistrar;
import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @ClassName KafkaConfigurationRegistrar
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:38
 */
public class KafkaConfigurationRegistrar extends AbstractConfigurationRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AutowiredAnnotationConfig[] configs = super.parseAutowiredAnnotationConfigs(metadata, EnableKafkaConfiguration.class.getName());
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        Object processor = new KafkaAutowiredAnnotationProcessor(beanFactory, configs);
        beanFactory.registerSingleton(KafkaAutowiredAnnotationProcessor.CLASS_NAME, processor);
    }
}
