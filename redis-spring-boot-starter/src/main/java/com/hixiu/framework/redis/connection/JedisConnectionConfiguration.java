/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: JedisConnectionConfiguration
 * Author:   程建乐
 * Date:     2019/9/1 0:43
 * Description: JedisConnectionConfiguration
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis.connection;

import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈JedisConnectionConfiguration〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class JedisConnectionConfiguration extends RedisConnectionConfiguration {
    private final RedisProperties properties;

    private final List<JedisClientConfigurationBuilderCustomizer> builderCustomizers;

    public JedisConnectionConfiguration(RedisProperties properties) {
        super(properties, null, null);
        this.properties = properties;
        this.builderCustomizers = Collections.EMPTY_LIST;
    }

    public JedisClientConfiguration getJedisClientConfiguration() {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(
                JedisClientConfiguration.builder());
        RedisProperties.Pool pool = this.properties.getJedis().getPool();
        if (pool != null) {
            applyPooling(pool, builder);
        }
        if (StringUtils.hasText(this.properties.getUrl())) {
            customizeConfigurationFromUrl(builder);
        }
        customize(builder);
        return builder.build();
    }

    private JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        if (this.properties.isSsl()) {
            builder.useSsl();
        }
        if (this.properties.getTimeout() != null) {
            Duration timeout = this.properties.getTimeout();
            builder.readTimeout(timeout).connectTimeout(timeout);
        }
        return builder;
    }

    private void applyPooling(RedisProperties.Pool pool, JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        builder.usePooling().poolConfig(jedisPoolConfig(pool));
    }

    private JedisPoolConfig jedisPoolConfig(RedisProperties.Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());

        System.out.println(pool.getClass());

        Method[] methods = pool.getClass().getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            System.out.println(methodName);
        }
        config.setTestOnReturn(Boolean.TRUE.equals(pool.getTestOnReturn()));
        config.setTestOnCreate(Boolean.TRUE.equals(pool.getTestOnCreate()));
        config.setTestOnBorrow(Boolean.TRUE.equals(pool.getTestOnBorrow()));
        if (pool.getMaxWait() != null) {
            config.setMaxWaitMillis(pool.getMaxWait().toMillis());
        }

        return config;
    }

    private void customizeConfigurationFromUrl(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        RedisConnectionConfiguration.ConnectionInfo connectionInfo = parseUrl(this.properties.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    private void customize(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        for (JedisClientConfigurationBuilderCustomizer customizer : this.builderCustomizers) {
            customizer.customize(builder);
        }
    }
}
