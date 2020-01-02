package com.hixiu.framework.kafka.annotation;

import com.hixiu.framework.common.inject.AutowiredBean;
import com.hixiu.framework.kafka.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;

/**
 * @ClassName KafkaAutowiredBean
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:01
 */
public class KafkaAutowiredBean extends AutowiredBean<KafkaProperties> {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaAutowiredBean.class);

    private RecordMessageConverter messageConverter;
    /**
     * Producer
     */
    private ProducerListener producerListener;
    private ProducerFactory<String, String> producerFactory;
    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaProducer kafkaProducer;
    /**
     * Consumer
     */
    private ConsumerFactory<String, String> consumerFactory;
    private ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory;

    public KafkaAutowiredBean(String namespace, KafkaProperties properties) {
        super(namespace, properties);
    }

    public RecordMessageConverter getMessageConverter() {
        return messageConverter;
    }

    void setMessageConverter(RecordMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public ProducerListener getProducerListener() {
        return producerListener;
    }

    void setProducerListener(ProducerListener producerListener) {
        this.producerListener = producerListener;
    }

    public ProducerFactory<String, String> getProducerFactory() {
        return producerFactory;
    }

    void setProducerFactory(ProducerFactory<String, String> producerFactory) {
        this.producerFactory = producerFactory;
    }

    public KafkaTemplate<String, String> getKafkaTemplate() {
        return kafkaTemplate;
    }

    void setKafkaTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public KafkaProducer getKafkaProducer() {
        return kafkaProducer;
    }

    void setKafkaProducer(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public ConsumerFactory<String, String> getConsumerFactory() {
        return consumerFactory;
    }

    void setConsumerFactory(ConsumerFactory<String, String> consumerFactory) {
        this.consumerFactory = consumerFactory;
    }

    public ConcurrentKafkaListenerContainerFactory<String, String> getListenerContainerFactory() {
        return listenerContainerFactory;
    }

    void setListenerContainerFactory(ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory) {
        this.listenerContainerFactory = listenerContainerFactory;
    }

    @Override
    public synchronized void destroy() {
        if (producerFactory instanceof DefaultKafkaProducerFactory) {
            LOGGER.info("Shutting Down KafkaProducerFactory . . .");
            try {
                ((DefaultKafkaProducerFactory<String, String>) producerFactory).stop();
            } catch (Exception e) {
                LOGGER.warn("Shut Down KafkaProducerFactory Failed", e);
            }
            producerFactory = null;
        }
    }
}
