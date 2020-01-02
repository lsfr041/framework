package org.springframework.kafka.listener.adapter;

import com.hixiu.framework.kafka.KafkaConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.BatchAcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaNull;
import org.springframework.kafka.support.converter.BatchMessageConverter;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName BatchMessagingMessageListenerAdapter
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:00
 */
public class BatchMessagingMessageListenerAdapter<K, V> extends MessagingMessageListenerAdapter<K, V>
        implements BatchAcknowledgingConsumerAwareMessageListener<K, V> {
    private static final Message<KafkaNull> NULL_MESSAGE = new GenericMessage<>(KafkaNull.INSTANCE);

    private BatchMessageConverter batchMessageConverter = new BatchMessagingMessageConverter();

    private KafkaListenerErrorHandler errorHandler;

    public BatchMessagingMessageListenerAdapter(Object bean, Method method) {
        this(bean, method, null);
    }

    public BatchMessagingMessageListenerAdapter(Object bean, Method method, KafkaListenerErrorHandler errorHandler) {
        super(bean, method);
        this.errorHandler = errorHandler;
    }

    /**
     * Set the BatchMessageConverter.
     * @param messageConverter the converter.
     */
    public void setBatchMessageConverter(BatchMessageConverter messageConverter) {
        this.batchMessageConverter = messageConverter;
        if (messageConverter.getRecordMessageConverter() != null) {
            setMessageConverter(messageConverter.getRecordMessageConverter());
        }
    }

    /**
     * Return the {@link BatchMessagingMessageConverter} for this listener,
     * being able to convert {@link Message}.
     * @return the {@link BatchMessagingMessageConverter} for this listener,
     * being able to convert {@link Message}.
     */
    protected final BatchMessageConverter getBatchMessageConverter() {
        return this.batchMessageConverter;
    }

    /**
     * Kafka {@link MessageListener} entry point.
     * <p> Delegate the message to the target listener method,
     * with appropriate conversion of the message argument.
     * @param records the incoming list of Kafka {@link ConsumerRecord}.
     * @param acknowledgment the acknowledgment.
     * @param consumer the consumer.
     */
    @Override
    public void onMessage(List<ConsumerRecord<K, V>> records, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        Message<?> message;
        if (!isConsumerRecordList()) {
            if (isMessageList()) {
                List<Message<?>> messages = new ArrayList<>(records.size());
                for (ConsumerRecord<K, V> record : records) {
                    messages.add(toMessagingMessage(record, acknowledgment, consumer));
                }
                message = MessageBuilder.withPayload(messages).build();
            }
            else {
                message = toMessagingMessage(records, acknowledgment, consumer);
            }
        }
        else {
            message = NULL_MESSAGE; // optimization since we won't need any conversion to invoke
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Processing [" + message + "]");
        }

      //  String groupId = ((consumer != null) ? ((KafkaConsumer) consumer).getGroupId() : "");
        //String topic = CollectionUtils.isEmpty(records)?"":records.get(0).topic();
      //  Transaction transaction = Cat.newTransaction("MafkaRecvBathMessage", (topic + ":" + groupId));
        try{
            try {
                Object result = invokeHandler(records, acknowledgment, message, consumer);
                if (result != null) {
                    handleResult(result, records, message);
                }
            }
            catch (ListenerExecutionFailedException e) {
                if (this.errorHandler != null) {
                    try {
                        if (message.equals(NULL_MESSAGE)) {
                            message = new GenericMessage<>(records);
                        }
                        Object result = this.errorHandler.handleError(message, e, consumer);
                        if (result != null) {
                            handleResult(result, records, message);
                        }
                    }
                    catch (Exception ex) {
                        throw new ListenerExecutionFailedException(createMessagingErrorMessage(
                                "Listener error handler threw an exception for the incoming message",
                                message.getPayload()), ex);
                    }
                }
                else {
                    throw e;
                }
            }
            //transaction.setStatus(Transaction.SUCCESS);
        }catch (Throwable e){
          //  transaction.setStatus(e);
            throw e;
        }finally {
           // transaction.complete();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Message<?> toMessagingMessage(List records, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
        return getBatchMessageConverter().toMessage(records, acknowledgment, consumer, getType());
    }
}
