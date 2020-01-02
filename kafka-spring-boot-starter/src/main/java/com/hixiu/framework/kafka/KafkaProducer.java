package com.hixiu.framework.kafka;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName KafkaProducer
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:03
 */
public class KafkaProducer {

    public static final String FRAMEWORK_IDEMPOTENCE_KEY = "$F_I_Key_";//为了减少key长度，前缀使用缩写

    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        if (kafkaTemplate == null) {
            throw new RuntimeException("请指定有效的KafkTemplate对象");
        }
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean isTransactional() {
        return this.kafkaTemplate.isTransactional();
    }

    public ListenableFuture<SendResult<String, String>> sendDefault(String data) {
        return this.send(null,null,null,null, data);
    }

    public ListenableFuture<SendResult<String, String>> sendDefault(String key, String data) {
        return this.send(null,null,null,key, data);
    }

    public ListenableFuture<SendResult<String, String>> sendDefault(Integer partition, String key, String data) {
        return this.send(null,partition,null,key, data);
    }

    public ListenableFuture<SendResult<String, String>> sendDefault(Integer partition, Long timestamp, String key, String data) {
        return this.send(null,partition,timestamp,key, data);
    }

    public ListenableFuture<SendResult<String, String>> send(String topic, String data) {
        return this.send(topic,null,null,null, data);
    }

    public ListenableFuture<SendResult<String, String>> send(String topic, String key, String data) {
        return this.send(topic,null,null,key, data);
    }

    public ListenableFuture<SendResult<String, String>> send(String topic, Integer partition, String key, String data) {
        return this.send(topic,partition,null,key, data);
    }

    public ListenableFuture<SendResult<String, String>> send(String topic, Integer partition, Long timestamp, String key, String data) {
        //如果key为空，则构造唯一key
        return this.kafkaTemplate.send(topic, partition, timestamp, buildKey(key), data);
    }

    public ListenableFuture<SendResult<String, String>> send(ProducerRecord<String, String> record) {
        if(StringUtils.isEmpty(record.key())){
            return this.kafkaTemplate.send(new ProducerRecord<>(record.topic(), record.partition(), record.timestamp(), buildKey(record.key()), record.value(), record.headers()));
        }else{
            return this.kafkaTemplate.send(record);
        }
    }

    public ListenableFuture<SendResult<String, String>> send(Message<String> message) {
        return this.kafkaTemplate.send(message);
    }


    public List<PartitionInfo> partitionsFor(String topic) {
        return this.kafkaTemplate.partitionsFor(topic);
    }

    public Map<MetricName, ? extends Metric> metrics() {
        return this.kafkaTemplate.metrics();
    }

    public <T> T execute(KafkaOperations.ProducerCallback<String, String, T> callback) {
        return this.kafkaTemplate.execute(callback);
    }

    public <T> T executeInTransaction(KafkaOperations.OperationsCallback<String, String, T> callback) {
        return this.kafkaTemplate.executeInTransaction(callback);
    }

    public void flush() {
        this.kafkaTemplate.flush();
    }

    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets) {
        this.kafkaTemplate.sendOffsetsToTransaction(offsets);
    }

    public void sendOffsetsToTransaction(Map<TopicPartition, OffsetAndMetadata> offsets, String consumerGroupId) {
        this.kafkaTemplate.sendOffsetsToTransaction(offsets, consumerGroupId);
    }

    public String buildKey(String key){
        return StringUtils.isEmpty(key) ? (FRAMEWORK_IDEMPOTENCE_KEY + UUID.randomUUID().toString().replaceAll("-","")) : key;
    }
}
