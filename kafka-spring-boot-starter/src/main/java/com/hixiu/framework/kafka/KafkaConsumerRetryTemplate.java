package com.hixiu.framework.kafka;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.hixiu.framework.redis.RedisService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.RetryState;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;

import static org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.CONTEXT_CONSUMER;
import static org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.CONTEXT_RECORD;

/**
 * @ClassName KafkaConsumerRetryTemplate
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:35
 */
public class KafkaConsumerRetryTemplate  extends RetryTemplate {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerRetryTemplate.class);

    private RedisService redisService;

    private Config config = null;

    public KafkaConsumerRetryTemplate(RedisService redisService) {
        this(redisService,null);
    }
    public KafkaConsumerRetryTemplate(RedisService redisService,String namespace){
        this.redisService = redisService;
        if(StringUtils.isEmpty(namespace)){
            return;
        }
        config = ConfigService.getConfig(namespace);
    }
    public Long getRedisExpire(){
        Long defaultExpire = -1L;//默认是无效值，会使用redis中配置的默认值
        if(config == null){
            return defaultExpire;
        }else{
            return config.getLongProperty("starter.kafka.redis.expire",defaultExpire);
        }
    }

    @Override
    protected void close(RetryPolicy retryPolicy, RetryContext context, RetryState state, boolean succeeded) {
        try {
            Boolean isRecovered = (Boolean) context.getAttribute(RetryContext.RECOVERED); // 是否调用了兜底逻辑
            if (isRecovered == null || isRecovered == false) { // 如果未调用兜底逻辑则认为消息消费成功
                ConsumerRecord<String, String> record = (ConsumerRecord<String, String>) context.getAttribute(CONTEXT_RECORD);
                LOGGER.trace("Kafka消息消费成功:{}", record);
                if (redisService != null) {
                    KafkaConsumer consumer = (KafkaConsumer) context.getAttribute(CONTEXT_CONSUMER);
                    Long expire = getRedisExpire();
                    if(expire >= 0){
                        redisService.set(buildCacheKey(record,consumer), "true" , expire);
                    }else{
                        redisService.set(buildCacheKey(record,consumer), "true");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Kafka消息自动幂等处理失败", e);
        }
        super.close(retryPolicy, context, state, succeeded);
    }

    /**
     * 构建消息幂等缓存key
     *
     * @param record
     * @return
     */
    protected String buildCacheKey(ConsumerRecord<?, ?> record,KafkaConsumer kafkaConsumer) {
        if((!StringUtils.isEmpty(record.key())) && record.key().toString().indexOf(KafkaProducer.FRAMEWORK_IDEMPOTENCE_KEY) == 0){
            //避免消息重复发送导致的重复消费
            return String.format("$KR_ID_%s_%s_%s", record.topic(), record.key(), ((kafkaConsumer != null)?kafkaConsumer.getGroupId():"") );
        }else{
            //避免重复消费
            return String.format("$KR_ID_%s_%d_%d_%s", record.topic(), record.partition(), record.offset(), ((kafkaConsumer != null)?kafkaConsumer.getGroupId():"") );
        }
    }

    /**
     * 指定消息是否已经成功消费过
     *
     * @param record
     * @return
     */
    public boolean isConsumed(ConsumerRecord<?, ?> record,KafkaConsumer kafkaConsumer) {
        return record == null || (redisService != null && redisService.hasKey(buildCacheKey(record,kafkaConsumer)));
    }
}
