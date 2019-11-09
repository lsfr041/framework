/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisService
 * Author:   程建乐
 * Date:     2019/9/1 0:52
 * Description:
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public interface RedisService {
    /**
     * 设置缓存过期时间
     *
     * @param key
     * @param time
     * @return
     */
    public boolean expire(String key, long time);

    /**
     * 获取缓存过期时间
     *
     * @param key
     * @return
     * @throws
     */
    public long getExpire(String key) throws RedisException;

    /**
     * 检查缓存key是否存在
     *
     * @param key
     * @return
     */
    public boolean hasKey(String key);

    /**
     * (批量)删除缓存
     *
     * @param key
     * @return
     */
    public int del(String... key);

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public Object get(String key);

    /**
     * 批量获取缓存
     *
     * @param keys
     * @return
     */
    public List<Object> mget(String... keys);

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, Object value);

    /**
     * 设置缓存
     *
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean set(String key, Object value, long time);

    /**
     * hash类型缓存递增
     *
     * @param key
     * @param delta
     * @return
     * @throws
     */
    public long incr(String key, long delta) throws RedisException;

    /**
     * hash类型缓存递减
     *
     * @param key
     * @param delta
     * @return
     * @throws
     */
    public long decr(String key, long delta) throws RedisException;

    /**
     * 获取hash类型缓存的某个子项
     *
     * @param key
     * @param item
     * @return
     */
    public Object hget(String key, String item);

    /**
     * 获取hash类型缓存所有子项
     *
     * @param key
     * @return
     */
    public Map<Object, Object> hmget(String key);

    /**
     * 设置hash类型缓存值
     *
     * @param key
     * @param map
     * @return
     */
    public boolean hmset(String key, Map<String, Object> map);

    /**
     * 设置hash类型缓存值
     *
     * @param key
     * @param map
     * @param time
     * @return
     */
    public boolean hmset(String key, Map<String, Object> map, long time);

    /**
     * 设置hash类型缓存某个子项的值
     *
     * @param key
     * @param item
     * @param value
     * @return
     */
    public boolean hset(String key, String item, Object value);

    /**
     * 设置hash类型缓存某个子项的值
     *
     * @param key
     * @param item
     * @param value
     * @param time
     * @return
     */
    public boolean hset(String key, String item, Object value, long time);

    /**
     * (批量)删除hash类型缓存子项
     *
     * @param key
     * @param item
     * @return
     */
    public boolean hdel(String key, String... item);

    /**
     * 检查hash类型缓存是否存在指定子项
     *
     * @param key
     * @param item
     * @return
     */
    public boolean hHasKey(String key, String item);

    /**
     * set类型缓存递增
     *
     * @param key
     * @param item
     * @param by
     * @return
     * @throws
     */
    public double hincr(String key, String item, double by) throws RedisException;

    /**
     * set类型缓存递减
     *
     * @param key
     * @param item
     * @param by
     * @return
     * @throws
     */
    public double hdecr(String key, String item, double by) throws RedisException;

    /**
     * 获取set类型缓存值
     *
     * @param key
     * @return
     */
    public Set<Object> sGet(String key);

    /**
     * 检查set类型缓存是否存在指定元素
     *
     * @param key
     * @param value
     * @return
     */
    public boolean sHasValue(String key, Object value);

    /**
     * 设置set类型缓存值
     *
     * @param key
     * @param values
     * @return
     */
    public long sSet(String key, Object... values);

    /**
     * 设置set类型缓存值
     *
     * @param key
     * @param time
     * @param values
     * @return
     */
    public long sSetAndTime(String key, long time, Object... values);

    /**
     * 获取set类型缓存元素数量
     *
     * @param key
     * @return
     */
    public long sGetSetSize(String key);

    /**
     * 移除set类型缓存指定元素
     *
     * @param key
     * @param values
     * @return
     */
    public long setRemove(String key, Object... values);

    /**
     * 获取list类型缓存指定区间元素
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<Object> lGet(String key, long start, long end);

    /**
     * 获取List类型缓存元素数量
     *
     * @param key
     * @return
     */
    public long lGetListSize(String key);

    /**
     * 获取List类型缓存某个元素
     *
     * @param key
     * @param index
     * @return
     */
    public Object lGetIndex(String key, long index);

    /**
     * 在List类型缓存后追加元素
     *
     * @param key
     * @param value
     * @return
     */
    public boolean lAdd(String key, Object value);

    /**
     * 在List类型缓存后追加元素
     *
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean lAdd(String key, Object value, long time);

    /**
     * 设置List类型缓存值
     *
     * @param key
     * @param value
     * @return
     */
    public boolean lAdd(String key, List<Object> value);

    /**
     * 设置List类型缓存值
     *
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean lAdd(String key, List<Object> value, long time);

    /**
     * 设置List类型缓存某个子项的值
     *
     * @param key
     * @param index
     * @param value
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value);

    /**
     * 移除List类型缓存指定头部元素
     *
     * @param key
     * @param count
     * @param value
     * @return
     */
    public long lRemove(String key, long count, Object value);

    /**
     * 获取RedisService核心
     *
     * @return
     */
    public RedisService getKernel();

    /**
     * setNX
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setNX(String key, Object value);

    /**
     * 获取一个锁对象
     *
     * @param key
     * @return
     */
    public RedisLocker buildLock(String key);

    /**
     * 获取一个锁对象
     *
     * @param key
     * @param expire
     * @param timeUnit
     * @return
     */
    public RedisLocker buildLock(String key, long expire, TimeUnit timeUnit);

    /**
     * 获取RedisTemplate对象，可以通过该对象调用未被RedisService暴露出来的redis方法
     *
     * @return RedisTemplate
     */
    public RedisTemplate getRedisTemplate();

    /**
     * 基于Redis的锁对象
     */
    interface RedisLocker {

        /**
         * 尝试获取锁，立即返回
         *
         * @return true=获取成功，false=获取失败
         */
        boolean tryLock();

        /**
         * 释放锁
         */
        void release();

        /**
         * 获取key
         */
        Object getKey();


    }
}
