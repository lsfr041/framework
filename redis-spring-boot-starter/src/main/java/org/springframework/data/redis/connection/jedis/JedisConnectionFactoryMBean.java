package org.springframework.data.redis.connection.jedis;

/**
 * @创建人:程建乐
 * @创建时间: 2019/9/1
 * @描述
 */
public interface JedisConnectionFactoryMBean {
    int getNumActive();

    int getNumIdle();

    int getNumWaiters();
}
