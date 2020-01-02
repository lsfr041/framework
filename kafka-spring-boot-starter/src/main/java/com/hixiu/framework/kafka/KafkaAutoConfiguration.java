package com.hixiu.framework.kafka;

import com.hixiu.framework.kafka.annotation.KafkaAutowiredAnnotationProcessor;
import com.hixiu.framework.kafka.annotation.KafkaListenerAnnotationProcessor;
import com.hixiu.framework.redis.RedisAutoConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;
import org.springframework.kafka.config.KafkaListenerConfigUtils;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

/**
 * @ClassName KafkaAutoConfiguration
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:47
 */
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class KafkaAutoConfiguration {

    @Bean(name = KafkaAutowiredAnnotationProcessor.CLASS_NAME)
    @ConditionalOnMissingBean(name = KafkaAutowiredAnnotationProcessor.CLASS_NAME)
    public KafkaAutowiredAnnotationProcessor kafkaAutowiredAnnotationBeanPostProcessor() {
        return new KafkaAutowiredAnnotationProcessor();
    }

    @Bean(name = KafkaListenerConfigUtils.KAFKA_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public KafkaListenerAnnotationProcessor kafkaListenerAnnotationProcessor() {
        return new KafkaListenerAnnotationProcessor();
    }

    @Bean(name = KafkaListenerConfigUtils.KAFKA_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
    public KafkaListenerEndpointRegistry defaultKafkaListenerEndpointRegistry() {
        return new KafkaListenerEndpointRegistry();
    }
}
