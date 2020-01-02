package com.hixiu.framework.kafka;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Map;

/**
 * @ClassName KafkaConsumerFactory
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:37
 */
public class KafkaConsumerFactory<K, V> extends DefaultKafkaConsumerFactory<K, V> {

    public KafkaConsumerFactory(Map<String, Object> configs) {
        super(configs);
    }

    protected KafkaConsumer<K, V> createKafkaConsumer(Map<String, Object> configs) {
        return new KafkaConsumer<>(configs, getKeyDeserializer(), getValueDeserializer());
    }
}
