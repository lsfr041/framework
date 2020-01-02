package com.hixiu.framework.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;

import static org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter.CONTEXT_RECORD;

/**
 * 消费者重试机制的兜底回调<br/>
 * <p>
 * see {@link org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter}
 * see {@link org.springframework.kafka.config.AbstractKafkaListenerEndpoint}
 * see {@link org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer}
 *
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:36
 */
public class KafkaConsumerRecoveryCallback  implements RecoveryCallback<Void> {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerRecoveryCallback.class);

    @Override
    public Void recover(RetryContext retryContext) {
        String message = String.format("Kafka消息消费失败[Retry=%s],%s", retryContext.getRetryCount(), retryContext.getAttribute(CONTEXT_RECORD));
        LOGGER.error(message, retryContext.getLastThrowable());
        return null;
    }
}
