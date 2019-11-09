/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisAutowiredBeanRegistrar
 * Author:   程建乐
 * Date:     2019/9/1 0:49
 * Description: RedisAutowiredBeanRegistrar
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import com.hixiu.framework.common.inject.AbstractAutowiredBeanRegistrar;
import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import com.hixiu.framework.common.properties.ApolloConfigurationPropertiesBinderUtils;
import com.hixiu.framework.common.utils.NameSpaceBeanNameUtils;
import com.hixiu.framework.redis.connection.JedisConnectionConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactoryMBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler;
import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisAutowiredBeanRegistrar〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisAutowiredBeanRegistrar extends AbstractAutowiredBeanRegistrar<RedisAutowiredBean> {

    public RedisAutowiredBeanRegistrar(AutowiredAnnotationConfig config, DefaultListableBeanFactory beanFactory) {
        super(config, beanFactory);
        this.autowiredBean = new RedisAutowiredBean(config.getValue(), bindProperties(beanFactory, config.getValue()));
    }

    /**
     * 从配置中心绑定配置属性到{@link HixiuRedisProperties}
     *
     * @param beanFactory
     * @param namespace
     * @return
     */
    protected static HixiuRedisProperties bindProperties(DefaultListableBeanFactory beanFactory, String namespace) {
        return ApolloConfigurationPropertiesBinderUtils.bindFromNamespace(beanFactory, namespace, new HixiuRedisProperties());
    }

    /**
     * 创建Redis连接工厂
     *
     * @return
     * @see {@link JedisConnectionFactory}
     */
    protected void buildAndRegisterConnectionFactory() {
        String beanName = NameSpaceBeanNameUtils.build(JedisConnectionFactory.class, this.autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setConnectionFactory(beanFactory.getBean(beanName, JedisConnectionFactory.class));
        } else {
            logger.info("创建并注册Bean:{}", beanName);
            try {
                JedisConnectionConfiguration configuration = new JedisConnectionConfiguration(autowiredBean.getProperties());
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JedisConnectionFactory.class);
                builder.addConstructorArgValue(configuration.getStandaloneConfig());
                builder.addConstructorArgValue(configuration.getJedisClientConfiguration());
                registerBeanDefinition(beanName, builder.getBeanDefinition());
                this.autowiredBean.setConnectionFactory(beanFactory.getBean(beanName, JedisConnectionFactory.class));
            } catch (Throwable e) {
                throw new RedisException("创建Redis连接工厂失败", e);
            }
        }
    }

    /**
     * 将连接池暴露到jmx
     */
    protected void buildAndRegisterJmxExporter() {
        HixiuRedisProperties properties = this.autowiredBean.getProperties();
        JedisConnectionFactory connectionFactory = this.autowiredBean.getConnectionFactory();
        if (properties == null || connectionFactory == null) {
            logger.warn("配置信息或连接工厂为空");
            return;
        }
        if (properties.isEnableExportJmx()) {
            String objectName = "com.hixiu.framework:type=JedisConnectionFactory,id=" + this.autowiredBean.getNamespace();
            logger.info("准备将连接池信息暴露到JMX:" + objectName);
            try {
                InterfaceBasedMBeanInfoAssembler assembler = new InterfaceBasedMBeanInfoAssembler();
                assembler.setManagedInterfaces(new Class<?>[]{JedisConnectionFactoryMBean.class});
                MBeanExporter exporter = new MBeanExporter();
                Map<String, Object> beans = new HashMap<>(1);
                beans.put(objectName, connectionFactory);
                exporter.setAssembler(assembler);

                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MBeanExporter.class);
                builder.addPropertyValue("beans", beans);
                builder.addPropertyValue("assembler", assembler);

                String beanName = NameSpaceBeanNameUtils.build(MBeanExporter.class, this.autowiredBean.getNamespace());
                registerBeanDefinition(beanName, builder.getBeanDefinition());
            } catch (Throwable e) {
                throw new RedisException("将连接池信息暴露到JMX失败", e);
            }
        } else {
            logger.info("JMX特性被禁用");
        }
    }

    /**
     * 创建redis template
     *
     * @see {@link RedisTemplate}
     * @see {@link RedisSerializer}
     * @see {@link StringRedisSerializer}
     * @see {@link LimitJackson2JsonRedisSerializer}
     */
    protected void buildAndRegisterRedisTemplate() {
        String beanName = NameSpaceBeanNameUtils.build(RedisTemplate.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setRedisTemplate(beanFactory.getBean(beanName, RedisTemplate.class));
        } else {
            logger.info("创建并注册Bean:{}", beanName);
            try {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RedisTemplate.class);
                //  builder.addConstructorArgValue(autowiredBean.getNamespace());
                RedisSerializer keySerializer = new StringRedisSerializer();
                builder.addPropertyValue("keySerializer", keySerializer);
                builder.addPropertyValue("hashKeySerializer", keySerializer);

                RedisSerializer valueSerializer = null;
                try {
                    valueSerializer = beanFactory.getBean(RedisSerializer.class);
                    logger.info("使用来自Spring容器的RedisSerializer");
                } catch (Exception e) {
                    logger.info("尝试从Spring容器获取RedisSerializer失败:{}", e.getMessage());
                }
                valueSerializer = valueSerializer != null ? valueSerializer : new LimitJackson2JsonRedisSerializer(autowiredBean.getProperties().getValueLimitBytes());
                builder.addPropertyValue("valueSerializer", valueSerializer);
                builder.addPropertyValue("hashValueSerializer", valueSerializer);
                builder.addPropertyValue("enableTransactionSupport", autowiredBean.getProperties().isEnableTransactionSupport());
                builder.addPropertyValue("connectionFactory", autowiredBean.getConnectionFactory());
                registerBeanDefinition(beanName, builder.getBeanDefinition());
                this.autowiredBean.setRedisTemplate(beanFactory.getBean(beanName, RedisTemplate.class));
            } catch (Throwable e) {
                throw new RedisException("创建RedisTemplate失败", e);
            }
        }
    }

    /**
     * 创建redis service
     *
     * @see {@link RedisService}
     * @see {@link RedisServiceImpl}
     * @see {@link RedisServiceDecorator}
     */
    protected void buildAndRegisterRedisService() {
        String beanName = NameSpaceBeanNameUtils.build(RedisService.class, autowiredBean.getNamespace());
        if (beanFactory.containsBean(beanName)) {
            logger.info("Bean[{}]已存在", beanName);
            this.autowiredBean.setRedisService(beanFactory.getBean(beanName, RedisService.class));
        } else {
            logger.info("创建并注册Bean:{}", beanName);
            try {
                BeanDefinitionBuilder builder;
                HixiuRedisProperties.LocalCache localCache = autowiredBean.getProperties().getLocalCache();
                if (localCache != null && localCache.isEnable()) { // 如果开启本地辅助缓存
                    builder = BeanDefinitionBuilder.genericBeanDefinition(RedisServiceDecorator.class);
                    builder.addConstructorArgValue(localCache);
                    builder.addConstructorArgValue(new RedisServiceImpl(autowiredBean.getProperties(), autowiredBean.getRedisTemplate()));
                } else {
                    builder = BeanDefinitionBuilder.genericBeanDefinition(RedisServiceImpl.class);
                    builder.addConstructorArgValue(autowiredBean.getProperties());
                    builder.addConstructorArgValue(autowiredBean.getRedisTemplate());
                }
                registerBeanDefinition(beanName, builder.getBeanDefinition());
                this.autowiredBean.setRedisService(beanFactory.getBean(beanName, RedisService.class));
            } catch (Throwable e) {
                throw new RedisException("创建RedisService失败", e);
            }
        }
    }

    @Override
    protected RedisAutowiredBean build() {
        this.buildAndRegisterConnectionFactory();
        this.buildAndRegisterJmxExporter();
        this.buildAndRegisterRedisTemplate();
        this.buildAndRegisterRedisService();
        return autowiredBean;
    }

    public static RedisAutowiredBean register(AutowiredAnnotationConfig config, DefaultListableBeanFactory beanFactory) {
        return new RedisAutowiredBeanRegistrar(config, beanFactory).register();
    }
}
