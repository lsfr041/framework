package org.springframework.boot.autoconfigure.kafka;


import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import java.time.Duration;

/**
 * @ClassName ConcurrentKafkaListenerContainerFactoryConfigurer
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 15:31
 */
public class ConcurrentKafkaListenerContainerFactoryConfigurer {

    private KafkaProperties properties;
    private RecordMessageConverter messageConverter;
    private KafkaTemplate<String, String> replyTemplate;

    /**
     * Set the {@link KafkaProperties} to use.
     *
     * @param properties the properties
     */
    public void setKafkaProperties(KafkaProperties properties) {
        this.properties = properties;
    }

    /**
     * Set the {@link RecordMessageConverter} to use.
     *
     * @param messageConverter the message converter
     */
    public void setMessageConverter(RecordMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Set the {@link KafkaTemplate} to use to send replies.
     *
     * @param replyTemplate the reply template
     */
    public void setReplyTemplate(KafkaTemplate<String, String> replyTemplate) {
        this.replyTemplate = replyTemplate;
    }

    /**
     * Configure the specified Kafka listener container factory. The factory can be
     * further tuned and default settings can be overridden.
     *
     * @param listenerFactory the {@link ConcurrentKafkaListenerContainerFactory} instance
     *                        to configure
     * @param consumerFactory the {@link ConsumerFactory} to use
     */
    public void configure(
            ConcurrentKafkaListenerContainerFactory<String, String> listenerFactory,
            ConsumerFactory<String, String> consumerFactory) {
        listenerFactory.setConsumerFactory(consumerFactory);
        configureListenerFactory(listenerFactory);
        configureContainer(listenerFactory.getContainerProperties());
    }

    private void configureListenerFactory(
            ConcurrentKafkaListenerContainerFactory<String, String> factory) {
        PropertyMapper map = PropertyMapper.get();
        KafkaProperties.Listener properties = this.properties.getListener();
        map.from(properties::getConcurrency).whenNonNull().to(factory::setConcurrency);
        map.from(() -> this.messageConverter).whenNonNull()
                .to(factory::setMessageConverter);
        map.from(() -> this.replyTemplate).whenNonNull().to(factory::setReplyTemplate);
        map.from(properties::getType).whenEqualTo(KafkaProperties.Listener.Type.BATCH)
                .toCall(() -> factory.setBatchListener(true));
    }

    private void configureContainer(ContainerProperties container) {
        PropertyMapper map = PropertyMapper.get();
        KafkaProperties.Listener properties = this.properties.getListener();
        map.from(properties::getAckMode).whenNonNull().to(container::setAckMode);
        map.from(properties::getClientId).whenNonNull().to(container::setClientId);
        map.from(properties::getAckCount).whenNonNull().to(container::setAckCount);
        map.from(properties::getAckTime).whenNonNull().as(Duration::toMillis)
                .to(container::setAckTime);
        map.from(properties::getPollTimeout).whenNonNull().as(Duration::toMillis)
                .to(container::setPollTimeout);
        map.from(properties::getNoPollThreshold).whenNonNull()
                .to(container::setNoPollThreshold);
        map.from(properties::getIdleEventInterval).whenNonNull().as(Duration::toMillis)
                .to(container::setIdleEventInterval);
        map.from(properties::getMonitorInterval).whenNonNull().as(Duration::getSeconds)
                .as(Number::intValue).to(container::setMonitorInterval);
        map.from(properties::getLogContainerConfig).whenNonNull()
                .to(container::setLogContainerConfig);
    }
}
