package com.hixiu.framework.kafka.annotation;

import com.ctrip.framework.foundation.Foundation;
import com.hixiu.framework.common.inject.AbstractAutowiredBeanRegistrar;
import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import com.hixiu.framework.common.properties.ApolloConfigurationPropertiesBinderUtils;
import com.hixiu.framework.common.utils.NameSpaceBeanNameUtils;
import com.hixiu.framework.kafka.KafkaConsumerFactory;
import com.hixiu.framework.kafka.KafkaConsumerRecoveryCallback;
import com.hixiu.framework.kafka.KafkaConsumerRetryTemplate;
import com.hixiu.framework.kafka.KafkaProducer;
import com.hixiu.framework.redis.RedisAutowiredBeanRegistrar;
import com.hixiu.framework.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StringUtils;

/**
 * @ClassName KafkaAutowiredBeanRegistrar
 * @Description
 * @Author chengjianle
 * @Date 2020-01-02 16:09
 */
@Scope()
public class KafkaAutowiredBeanRegistrar extends AbstractAutowiredBeanRegistrar<KafkaAutowiredBean> {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaAutowiredBeanRegistrar.class);

    private RedisService redisService;
    private boolean isConsumer;
    private boolean batchListener = false;
    private static final String BATCH_CONSUMER_NAMESPACE = "middleware.kafka-batch";

    public KafkaAutowiredBeanRegistrar(AutowiredAnnotationConfig config, DefaultListableBeanFactory beanFactory) {
        super(config, beanFactory);
        this.autowiredBean = new KafkaAutowiredBean(config.getValue(), bindProperties(beanFactory, config.getValue()));
    }

    public KafkaAutowiredBeanRegistrar(AutowiredAnnotationConfig config, RedisService redisService, DefaultListableBeanFactory beanFactory) {
        super(config, beanFactory);
        KafkaProperties kafkaProperties = bindProperties(beanFactory, config.getValue());
        this.autowiredBean = new KafkaAutowiredBean(config.getValue(), kafkaProperties);
        this.redisService = redisService;
        this.isConsumer = true;
        if (BATCH_CONSUMER_NAMESPACE.equals(autowiredBean.getNamespace())) {
            this.batchListener = true;
        } else {
            this.batchListener = false;
        }
    }

    /**
     * 从配置中心绑定配置属性到{@link KafkaProperties}
     *
     * @param beanFactory
     * @param namespace
     * @return
     */
    protected static KafkaProperties bindProperties(DefaultListableBeanFactory beanFactory, String namespace) {
        KafkaProperties properties = ApolloConfigurationPropertiesBinderUtils.bindFromNamespace(beanFactory, namespace, new KafkaProperties());
        boolean notSetClientId = StringUtils.isEmpty(properties.getClientId());
        boolean notSetConsumerGroupId = StringUtils.isEmpty(properties.getConsumer().getGroupId());
        if (notSetClientId && notSetConsumerGroupId) { // 如果没有指定client id和consumer group id
            properties.setClientId(Foundation.app().getAppId()); // 则使用apollo app id
            properties.getConsumer().setGroupId(properties.getClientId()); // 则使用apollo app id
        } else if (notSetConsumerGroupId) { // 如果指定了client id但是未指定consumer group id
            properties.getConsumer().setGroupId(properties.getClientId()); // 则使用client id
        } else if (notSetClientId) { // 如果指定了consumer group id但是未指定client id
            properties.setClientId(properties.getConsumer().getGroupId()); // 则使用consumer group id
        }
        return properties;
    }

    /**
     * 创建kafka template
     *
     * @return
     */
    protected void buildAndRegisterKafkaTemplate() {
        String beanName = NameSpaceBeanNameUtils.build(KafkaTemplate.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setKafkaTemplate(beanFactory.getBean(beanName, KafkaTemplate.class));
        } else {
            logger.debug("创建并注册Bean:{}", beanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(KafkaTemplate.class);
            // builder.addConstructorArgValue(autowiredBean.getProducerFactory()).addConstructorArgValue(autowiredBean.getNamespace());
            builder.addConstructorArgValue(autowiredBean.getProducerFactory());
            if (autowiredBean.getMessageConverter() != null) {
                builder.addPropertyValue("messageConverter", autowiredBean.getMessageConverter());
            }
            builder.addPropertyValue("producerListener", autowiredBean.getProducerListener());
            builder.addPropertyValue("defaultTopic", autowiredBean.getProperties().getTemplate().getDefaultTopic());
            registerBeanDefinition(beanName, builder.getBeanDefinition());
            this.autowiredBean.setKafkaTemplate(beanFactory.getBean(beanName, KafkaTemplate.class));
        }
    }

    /**
     * 创建ProducerFactory
     */
    protected void buildAndRegisterProducerFactory() {
        String beanName = NameSpaceBeanNameUtils.build(ProducerFactory.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setProducerFactory(beanFactory.getBean(beanName, ProducerFactory.class));
        } else {
            logger.debug("创建并注册Bean:{}", beanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DefaultKafkaProducerFactory.class);
            builder.addConstructorArgValue(autowiredBean.getProperties().buildProducerProperties());
            registerBeanDefinition(beanName, builder.getBeanDefinition());
            this.autowiredBean.setProducerFactory(beanFactory.getBean(beanName, DefaultKafkaProducerFactory.class));
        }
    }

    protected void buildAndRegisterProducerListener() {
        String beanName = NameSpaceBeanNameUtils.build(ProducerListener.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setProducerListener(beanFactory.getBean(beanName, ProducerListener.class));
        } else {
            logger.debug("创建并注册Bean:{}", beanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(LoggingProducerListener.class);
            registerBeanDefinition(beanName, builder.getBeanDefinition());
            this.autowiredBean.setProducerListener(beanFactory.getBean(beanName, ProducerListener.class));
        }
    }

    protected void buildAndRegisterKafkaProducer() {
        String beanName = NameSpaceBeanNameUtils.build(KafkaProducer.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setKafkaProducer(beanFactory.getBean(beanName, KafkaProducer.class));
        } else {
            logger.debug("创建并注册Bean:{}", beanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(KafkaProducer.class);
            builder.addConstructorArgValue(autowiredBean.getKafkaTemplate());
            registerBeanDefinition(beanName, builder.getBeanDefinition());
            this.autowiredBean.setKafkaProducer(beanFactory.getBean(beanName, KafkaProducer.class));
        }
    }

    protected void buildAndRegisterListenerContainerFactory() {
        String beanName = NameSpaceBeanNameUtils.build(ConcurrentKafkaListenerContainerFactory.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setListenerContainerFactory(beanFactory.getBean(beanName, ConcurrentKafkaListenerContainerFactory.class));
        } else {
            logger.debug("创建并注册Bean:{}", beanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConcurrentKafkaListenerContainerFactory.class);
            builder.addPropertyValue("batchListener", batchListener);
            //非批量支持幂等性验证或重试逻辑
            if (!batchListener) {
                RetryTemplate retryTemplate;
                if (redisService != null) {
                    logger.info("基于RedisService为Kafka开启幂等消费功能");
                    retryTemplate = new KafkaConsumerRetryTemplate(redisService, autowiredBean.getNamespace());
                } else {
                    retryTemplate = new RetryTemplate();
                }
                SimpleRetryPolicy retryPolicy = autowiredBean.getProperties().getConsumer().getRetryPolicy();
                if (retryPolicy == null || retryPolicy.getMaxAttempts() < 1) {
                    retryPolicy = new SimpleRetryPolicy(1); // 最大重试次数最小为1,即不重试
                }
                retryTemplate.setRetryPolicy(retryPolicy);
                builder.addPropertyValue("retryTemplate", retryTemplate);
                builder.addPropertyValue("recoveryCallback", new KafkaConsumerRecoveryCallback());
            }
            registerBeanDefinition(beanName, builder.getBeanDefinition());
            this.autowiredBean.setListenerContainerFactory(beanFactory.getBean(beanName, ConcurrentKafkaListenerContainerFactory.class));

            ConcurrentKafkaListenerContainerFactoryConfigurer configurer = new ConcurrentKafkaListenerContainerFactoryConfigurer();
            configurer.setKafkaProperties(autowiredBean.getProperties());
            configurer.setMessageConverter(autowiredBean.getMessageConverter());
            configurer.setReplyTemplate(autowiredBean.getKafkaTemplate());
            configurer.configure(this.autowiredBean.getListenerContainerFactory(), autowiredBean.getConsumerFactory());

        }
    }

    protected void buildAndRegisterConsumerFactory() {
        String beanName = NameSpaceBeanNameUtils.build(ConsumerFactory.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setConsumerFactory(beanFactory.getBean(beanName, ConsumerFactory.class));
        } else {
            logger.debug("创建并注册Bean:{}", beanName);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(KafkaConsumerFactory.class);
            builder.addConstructorArgValue(autowiredBean.getProperties().buildConsumerProperties());
            registerBeanDefinition(beanName, builder.getBeanDefinition());
            this.autowiredBean.setConsumerFactory(beanFactory.getBean(beanName, ConsumerFactory.class));
        }
    }

    @Override
    public KafkaAutowiredBean register() {
        KafkaAutowiredBean bean = super.register();
        if (isConsumer) {
            if (autowiredBean.getConsumerFactory() == null) {
                this.buildAndRegisterConsumerFactory();
            }
            if (autowiredBean.getListenerContainerFactory() == null) {
                this.buildAndRegisterListenerContainerFactory();
            }
        }
        return bean;
    }

    @Override
    protected KafkaAutowiredBean build() {
        this.buildAndRegisterProducerListener();
        this.buildAndRegisterProducerFactory();
        this.buildAndRegisterKafkaTemplate();
        this.buildAndRegisterKafkaProducer();
        return autowiredBean;
    }

    public static KafkaAutowiredBean registerProducer(AutowiredAnnotationConfig config, DefaultListableBeanFactory beanFactory) {
        return new KafkaAutowiredBeanRegistrar(config, beanFactory).register();
    }

    public static KafkaAutowiredBean registerConsumer(KafkaListener kafkaListener, DefaultListableBeanFactory beanFactory) {
        LOGGER.info("从Namespace[{}]下读取配置并创建Kafka Consumer", kafkaListener.namespace());
        RedisService redisService = null;
        if (!StringUtils.isEmpty(kafkaListener.redisNamespace())) {
            redisService = RedisAutowiredBeanRegistrar.register(new AutowiredAnnotationConfig(kafkaListener.redisNamespace()), beanFactory).getRedisService();
        }
        return new KafkaAutowiredBeanRegistrar(new AutowiredAnnotationConfig(kafkaListener.namespace()), redisService, beanFactory).register();
    }
}
