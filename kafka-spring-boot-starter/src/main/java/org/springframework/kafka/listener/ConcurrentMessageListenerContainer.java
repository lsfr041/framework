package org.springframework.kafka.listener;

import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.TopicPartitionInitialOffset;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @ClassName ConcurrentMessageListenerContainer
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 15:51
 */
public class ConcurrentMessageListenerContainer<K, V> extends AbstractMessageListenerContainer<K, V> {

    private final List<KafkaMessageListenerContainer<K, V>> containers = new ArrayList<>();
    private static final AtomicInteger CONSUMER_CLIENT_ID_SEQUENCE = new AtomicInteger(1);

    private int concurrency = 1;

    /**
     * Construct an instance with the supplied configuration properties.
     * The topic partitions are distributed evenly across the delegate
     * {@link KafkaMessageListenerContainer}s.
     *
     * @param consumerFactory     the consumer factory.
     * @param containerProperties the container properties.
     */
    public ConcurrentMessageListenerContainer(ConsumerFactory<K, V> consumerFactory,
                                              ContainerProperties containerProperties) {
        super(consumerFactory, containerProperties);
        Assert.notNull(consumerFactory, "A ConsumerFactory must be provided");
    }

    public int getConcurrency() {
        return this.concurrency;
    }

    /**
     * The maximum number of concurrent {@link KafkaMessageListenerContainer}s running.
     * Messages from within the same partition will be processed sequentially.
     *
     * @param concurrency the concurrency.
     */
    public void setConcurrency(int concurrency) {
        Assert.isTrue(concurrency > 0, "concurrency must be greater than 0");
        this.concurrency = concurrency;
    }

    /**
     * Return the list of {@link KafkaMessageListenerContainer}s created by
     * this container.
     *
     * @return the list of {@link KafkaMessageListenerContainer}s created by
     * this container.
     */
    public List<KafkaMessageListenerContainer<K, V>> getContainers() {
        return Collections.unmodifiableList(this.containers);
    }

    @Override
    public Collection<TopicPartition> getAssignedPartitions() {
        return this.containers.stream()
                .map(KafkaMessageListenerContainer::getAssignedPartitions)
                .filter(Objects::nonNull)
                .flatMap(assignedPartitions -> assignedPartitions.stream())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isContainerPaused() {
        boolean paused = isPaused();
        if (paused) {
            for (AbstractMessageListenerContainer<K, V> container : this.containers) {
                if (!container.isContainerPaused()) {
                    return false;
                }
            }
        }
        return paused;
    }

    @Override
    public Map<String, Map<MetricName, ? extends Metric>> metrics() {
        Map<String, Map<MetricName, ? extends Metric>> metrics = new HashMap<>();
        for (KafkaMessageListenerContainer<K, V> container : this.containers) {
            metrics.putAll(container.metrics());
        }
        return Collections.unmodifiableMap(metrics);
    }

    /*
     * Under lifecycle lock.
     */
    @Override
    protected void doStart() {
        if (!isRunning()) {
            ContainerProperties containerProperties = getContainerProperties();
            TopicPartitionInitialOffset[] topicPartitions = containerProperties.getTopicPartitions();
            if (topicPartitions != null
                    && this.concurrency > topicPartitions.length) {
                this.logger.warn("When specific partitions are provided, the concurrency must be less than or "
                        + "equal to the number of partitions; reduced from " + this.concurrency + " to "
                        + topicPartitions.length);
                this.concurrency = topicPartitions.length;
            }
            setRunning(true);

            for (int i = 0; i < this.concurrency; i++) {
                KafkaMessageListenerContainer<K, V> container;
                if (topicPartitions == null) {
                    container = new KafkaMessageListenerContainer<>(this, this.consumerFactory,
                            containerProperties);
                } else {
                    container = new KafkaMessageListenerContainer<>(this, this.consumerFactory,
                            containerProperties, partitionSubset(containerProperties, i));
                }
                String beanName = getBeanName();
                container.setBeanName((beanName != null ? beanName : "consumer") + "-" + i);
                if (getApplicationEventPublisher() != null) {
                    container.setApplicationEventPublisher(getApplicationEventPublisher());
                }
                container.setClientIdSuffix("-" + CONSUMER_CLIENT_ID_SEQUENCE.getAndIncrement());
                container.setAfterRollbackProcessor(getAfterRollbackProcessor());
                container.start();
                this.containers.add(container);
            }
        }
    }

    private TopicPartitionInitialOffset[] partitionSubset(ContainerProperties containerProperties, int i) {
        TopicPartitionInitialOffset[] topicPartitions = containerProperties.getTopicPartitions();
        if (this.concurrency == 1) {
            return topicPartitions;
        } else {
            int numPartitions = topicPartitions.length;
            if (numPartitions == this.concurrency) {
                return new TopicPartitionInitialOffset[]{topicPartitions[i]};
            } else {
                int perContainer = numPartitions / this.concurrency;
                TopicPartitionInitialOffset[] subset;
                if (i == this.concurrency - 1) {
                    subset = Arrays.copyOfRange(topicPartitions, i * perContainer, topicPartitions.length);
                } else {
                    subset = Arrays.copyOfRange(topicPartitions, i * perContainer, (i + 1) * perContainer);
                }
                return subset;
            }
        }
    }

    /*
     * Under lifecycle lock.
     */
    @Override
    protected void doStop(final Runnable callback) {
        final AtomicInteger count = new AtomicInteger();
        if (isRunning()) {
            setRunning(false);
            for (KafkaMessageListenerContainer<K, V> container : this.containers) {
                if (container.isRunning()) {
                    count.incrementAndGet();
                }
            }
            for (KafkaMessageListenerContainer<K, V> container : this.containers) {
                if (container.isRunning()) {
                    container.stop(new Runnable() {

                        @Override
                        public void run() {
                            if (count.decrementAndGet() <= 0) {
                                callback.run();
                            }
                        }

                    });
                }
            }
            this.containers.clear();
        }
    }

    @Override
    public void pause() {
        super.pause();
        this.containers.forEach(c -> c.pause());
    }

    @Override
    public void resume() {
        super.resume();
        this.containers.forEach(c -> c.resume());
    }

    @Override
    public String toString() {
        return "ConcurrentMessageListenerContainer [concurrency=" + this.concurrency + ", beanName="
                + this.getBeanName() + ", running=" + this.isRunning() + "]";
    }

}


