/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisAutowiredAnnotationProcessor
 * Author:   程建乐
 * Date:     2019/9/1 0:48
 * Description: RedisAutowiredAnnotationProcessor
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import com.hixiu.framework.common.inject.AutowiredAnnotationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisAutowiredAnnotationProcessor〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisAutowiredAnnotationProcessor extends AutowiredAnnotationProcessor<RedisAutowiredBean, RedisAutowired> {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisAutowiredAnnotationProcessor.class);

    public static final String CLASS_NAME = "com.hixiu.framework.redis.RedisAutowiredAnnotationProcessor";

    public RedisAutowiredAnnotationProcessor() {
        super(RedisAutowired.class);
    }

    public RedisAutowiredAnnotationProcessor(DefaultListableBeanFactory beanFactory, AutowiredAnnotationConfig... configs) {
        super(RedisAutowired.class, beanFactory, configs);
    }

    @Override
    protected AutowiredAnnotationConfig parseConfig(RedisAutowired autowired) {
        return new AutowiredAnnotationConfig(autowired.value());
    }

    @Override
    protected RedisAutowiredBean create(AutowiredAnnotationConfig config) {
        RedisAutowiredBean bean = RedisAutowiredBeanRegistrar.register(config, beanFactory);
        RedisConnection redisConnection = null;
        try {
            redisConnection = bean.getConnectionFactory().getConnection();
        }catch (Throwable e){
            LOGGER.error("Redis初始化时，获取连接失败...",e);
            throw new RedisException("Redis初始化时，获取连接失败...",e);
        }finally {
            if(redisConnection != null){
                try {
                    redisConnection.close();
                }catch (DataAccessException e){
                    LOGGER.error("Redis初始化时，关闭连接失败...",e);
                }
            }
        }
        return bean;
    }

    @Override
    protected Object getBean(RedisAutowiredBean autowiredBean, Class<?> autowiredType) {
        if (autowiredType == RedisService.class) {
            return autowiredBean.getRedisService();
        } else if (autowiredType == RedisTemplate.class) {
            return autowiredBean.getRedisTemplate();
        } else if (autowiredType == JedisConnectionFactory.class) {
            return autowiredBean.getConnectionFactory();
        } else if (autowiredType == HixiuRedisProperties.class || autowiredType == RedisProperties.class) {
            return autowiredBean.getProperties();
        } else if (autowiredType == RedisAutowiredBean.class) {
            return autowiredBean;
        }
        return null;
    }
}
