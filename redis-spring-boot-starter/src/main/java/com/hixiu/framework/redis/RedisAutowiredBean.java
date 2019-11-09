/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisAutowiredBean
 * Author:   程建乐
 * Date:     2019/9/1 0:48
 * Description: RedisAutowiredBean
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import com.hixiu.framework.common.inject.AutowiredBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisAutowiredBean〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisAutowiredBean extends AutowiredBean<HixiuRedisProperties> {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisAutowiredBean.class);

    private JedisConnectionFactory connectionFactory;
    private RedisTemplate<String, Object> redisTemplate;
    private RedisService redisService;

    public RedisAutowiredBean(String namespace, HixiuRedisProperties properties) {
        super(namespace, properties);
    }

    public JedisConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    void setConnectionFactory(JedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }

    void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisService getRedisService() {
        return redisService;
    }

    void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public synchronized void destroy() {
        if (connectionFactory != null) {
            LOGGER.info("Shutting Down JedisConnectionFactory . . .");
            try {
                connectionFactory.destroy();
            } catch (Exception e) {
                LOGGER.warn("Shut Down JedisConnectionFactory Failed", e);
            }
            connectionFactory = null;
        }
    }
}
