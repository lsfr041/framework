package com.hixiu.framework.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
import java.util.Properties;

/**
 * @ClassName KafkaConsumer
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:28
 */
public class KafkaConsumer<K, V> extends org.apache.kafka.clients.consumer.KafkaConsumer<K, V> {
    private String groupId;

    public KafkaConsumer(Map<String, Object> configs) {
        super(configs);
    }

    public KafkaConsumer(Map<String, Object> configs, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
        super(configs, keyDeserializer, valueDeserializer);
        groupId = (String) configs.get(ConsumerConfig.GROUP_ID_CONFIG);
    }

    public KafkaConsumer(Properties properties,
                         Deserializer<K> keyDeserializer,
                         Deserializer<V> valueDeserializer) {
        super(properties, keyDeserializer, valueDeserializer);
        groupId = (String) properties.get(ConsumerConfig.GROUP_ID_CONFIG);
    }

    public String getGroupId() {
        return groupId;
    }
}
