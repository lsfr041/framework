package org.springframework.kafka.annotation;

import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MessageMapping
@Documented
@Repeatable(KafkaListeners.class)
public @interface KafkaListener {

    /**
     * kafka consumer配置所在的config namespace
     *
     * @return
     */
    String namespace() default "middleware.kafka";

    /**
     * kafka consumer幂等依赖的redis服务配置
     *
     * @return
     */
    String redisNamespace() default "";

    /**
     * The unique identifier of the container managing for this endpoint.
     * <p>If none is specified an auto-generated one is provided.
     *
     * @return the {@code id} for the container managing for this endpoint.
     * @see org.springframework.kafka.config.KafkaListenerEndpointRegistry#getListenerContainer(String)
     */
    String id() default "";

    /**
     * The topics for this listener.
     * The entries can be 'topic name', 'property-placeholder keys' or 'expressions'.
     * Expression must be resolved to the topic name.
     * Mutually exclusive with {@link #topicPattern()} and {@link #topicPartitions()}.
     *
     * @return the topic names or expressions (SpEL) to listen to.
     */
    String[] topics() default {};

    /**
     * The topic pattern for this listener.
     * The entries can be 'topic name', 'property-placeholder keys' or 'expressions'.
     * Expression must be resolved to the topic pattern.
     * Mutually exclusive with {@link #topics()} and {@link #topicPartitions()}.
     *
     * @return the topic pattern or expression (SpEL).
     */
    String topicPattern() default "";

    /**
     * The topicPartitions for this listener.
     * Mutually exclusive with {@link #topicPattern()} and {@link #topics()}.
     *
     * @return the topic names or expressions (SpEL) to listen to.
     */
    TopicPartition[] topicPartitions() default {};

    /**
     * If provided, the listener container for this listener will be added to a bean
     * with this value as its name, of type {@code Collection<MessageListenerContainer>}.
     * This allows, for example, iteration over the collection to start/stop a subset
     * of containers.
     *
     * @return the bean name for the group.
     */
    String containerGroup() default "";

    /**
     * Set an {@link KafkaListenerErrorHandler} to invoke if the listener method throws
     * an exception.
     *
     * @return the error handler.
     * @since 1.3
     */
    String errorHandler() default "";

    /**
     * Override the {@code group.id} property for the consumer factory with this value
     * for this listener only.
     *
     * @return the group id.
     * @since 1.3
     */
    String groupId() default "";

    /**
     * When {@link #groupId() groupId} is not provided, use the {@link #id() id} (if
     * provided) as the {@code group.id} property for the consumer. Set to false, to use
     * the {@code group.id} from the consumer factory.
     *
     * @return false to disable.
     * @since 1.3
     */
    boolean idIsGroup() default true;

    /**
     * When provided, overrides the client id property in the consumer factory
     * configuration. A suffix ('-n') is added for each container instance to ensure
     * uniqueness when concurrency is used.
     *
     * @return the client id prefix.
     * @since 2.1.1
     */
    String clientIdPrefix() default "";

    /**
     * A pseudo bean name used in SpEL expressions within this annotation to reference
     * the current bean within which this listener is defined. This allows access to
     * properties and methods within the enclosing bean.
     * Default '__listener'.
     * <p>
     * Example: {@code topics = "#{__listener.topicList}"}.
     *
     * @return the pseudo bean name.
     * @since 2.1.2
     */
    String beanRef() default "__listener";
}
