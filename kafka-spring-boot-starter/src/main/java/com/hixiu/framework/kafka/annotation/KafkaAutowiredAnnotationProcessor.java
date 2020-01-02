package com.hixiu.framework.kafka.annotation;

import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import com.hixiu.framework.common.inject.AutowiredAnnotationProcessor;
import com.hixiu.framework.kafka.KafkaProducer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;

/**
 * @ClassName KafkaAutowiredAnnotationProcessor
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:05
 */
public class KafkaAutowiredAnnotationProcessor extends AutowiredAnnotationProcessor<KafkaAutowiredBean, KafkaAutowired> {

    public static final String CLASS_NAME = "com.hixiu.framework.kafka.annotation.KafkaAutowiredAnnotationProcessor";

    public KafkaAutowiredAnnotationProcessor() {
        super(KafkaAutowired.class);
    }

    public KafkaAutowiredAnnotationProcessor(DefaultListableBeanFactory beanFactory, AutowiredAnnotationConfig... configs) {
        super(KafkaAutowired.class, beanFactory, configs);
    }

    @Override
    protected AutowiredAnnotationConfig parseConfig(KafkaAutowired autowired) {
        return new AutowiredAnnotationConfig(autowired.value());
    }

    @Override
    protected KafkaAutowiredBean create(AutowiredAnnotationConfig config) {
        return KafkaAutowiredBeanRegistrar.registerProducer(config, beanFactory);
    }

    @Override
    protected Object getBean(KafkaAutowiredBean autowiredBean, Class<?> autowiredType) {
        if (autowiredType == KafkaProducer.class) {
            return autowiredBean.getKafkaProducer();
        } else if (autowiredType == KafkaProperties.class) {
            return autowiredBean.getProperties();
        } else if (autowiredType == ProducerListener.class) {
            return autowiredBean.getProducerListener();
        } else if (autowiredType == ProducerFactory.class) {
            return autowiredBean.getProducerFactory();
        } else if (autowiredType == KafkaTemplate.class) {
            return autowiredBean.getKafkaTemplate();
        } else if (autowiredType == KafkaAutowiredBean.class) {
            return autowiredBean;
        }
        return null;
    }

}
