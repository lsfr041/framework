/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisServiceDecorator
 * Author:   程建乐
 * Date:     2019/9/1 0:52
 * Description:
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
/**
 * 〈
 *   通过对Redis远程缓存 + Guava缓存进行统一装饰，实现规避缓存服务hotkey带来的高频次redis访问性能问题
 *  〉<br>
 * 〈〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisServiceDecorator implements RedisService {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisServiceDecorator.class);

    private RedisService redisService;
    private LoadingCache<CacheKey, CacheValue> localCache;
    /**
     * hash/list类型缓存key与其子项key的映射关系
     */
    private ConcurrentHashMap<String, List<CacheKey>> keyItemMap = new ConcurrentHashMap<>();

    public RedisServiceDecorator(HixiuRedisProperties.LocalCache localCacheConfig, RedisService redisService) {
        if (redisService == null) {
            throw new RedisException("请指定RedisService对象");
        }
        this.redisService = redisService;
        if (localCacheConfig == null || !localCacheConfig.isEnable()) return;
        localCacheConfig.setMaxSize(Math.max(localCacheConfig.getMaxSize(), 10)); // 限定maxSize最小为10
        localCacheConfig.setExpireAfterWrite(Math.max(localCacheConfig.getExpireAfterWrite(), 10)); // 限定最小超时10ms
        LOGGER.info("开启辅助缓存:{}", localCacheConfig);
        CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
        builder.maximumSize(localCacheConfig.getMaxSize());
        builder.expireAfterWrite(localCacheConfig.getExpireAfterWrite(), TimeUnit.MILLISECONDS);
        localCache = builder.build(new CacheLoader<CacheKey, CacheValue>() {
            @Override
            public CacheValue load(CacheKey key) {
                Object value = null;
                if (key instanceof HashCacheKey) { // Hash类型缓存
                    HashCacheKey hashCacheKey = (HashCacheKey) key;
                    if (StringUtils.isEmpty(hashCacheKey.getItem())) { // 取整个hash表
                        LOGGER.trace("读取远程Hash缓存:{}", hashCacheKey);
                        value = redisService.hmget(hashCacheKey.getKey());
                    } else { // 取单个元素
                        CacheValue cacheValue = localCache.getIfPresent(key); // 优先读取本地整体缓存
                        if (cacheValue == null) { // 从远程缓存取子项
                            LOGGER.trace("读取远程Hash缓存:{}", hashCacheKey);
                            value = redisService.hget(hashCacheKey.getKey(), hashCacheKey.getItem());
                        } else { // 从本地整体缓存中取子项
                            LOGGER.trace("在本地Hash缓存中查找子项:{}", hashCacheKey);
                            value = ((Map<Object, Object>) cacheValue.getValue()).get(hashCacheKey.getItem());
                        }
                        if (!keyItemMap.contains(hashCacheKey.getKey())) {
                            LOGGER.trace("初始化Hash缓存key-item映射关系: {}", hashCacheKey);
                            keyItemMap.put(hashCacheKey.getKey(), Collections.synchronizedList(new LinkedList<>()));
                        }
                        keyItemMap.get(hashCacheKey.getKey()).add(hashCacheKey);
                    }
                } else if (key instanceof SetCacheKey) { // Set类型缓存
                    LOGGER.trace("读取远程Set缓存:{}", key);
                    value = redisService.sGet(key.getKey());
                } else if (key instanceof ListCacheKey) { // List类型缓存
                    ListCacheKey listCacheKey = (ListCacheKey) key;
                    if (listCacheKey.getItem() != null) { // 取整个list表
                        CacheValue cacheValue = localCache.getIfPresent(key); // 优先读取本地整体缓存
                        if (cacheValue == null) { // 从远程缓存取子项
                            LOGGER.trace("读取远程List缓存子项:{}", listCacheKey);
                            value = redisService.lGetIndex(listCacheKey.getKey(), listCacheKey.getItem());
                        } else { // 从本地整体缓存中取子项
                            LOGGER.trace("在本地List缓存中查找子项:{}", listCacheKey);
                            List<Object> values = (List<Object>) cacheValue.getValue();
                            if (!CollectionUtils.isEmpty(values) && values.size() > listCacheKey.getItem()) {
                                value = values.get(listCacheKey.getItem().intValue());
                            }
                        }
                        if (!keyItemMap.contains(listCacheKey.getKey())) {
                            LOGGER.trace("初始化List缓存key-index映射关系:{}", listCacheKey);
                            keyItemMap.put(listCacheKey.getKey(), Collections.synchronizedList(new LinkedList<>()));
                        }
                        keyItemMap.get(listCacheKey.getKey()).add(listCacheKey);
                    }
                } else { // 普通类型缓存
                    LOGGER.trace("读取远程缓存:{}", key);
                    value = redisService.get(key.getKey());
                }
                return new CacheValue(value);
            }
        });
    }

    @Override
    public boolean expire(String key, long time) {
        return redisService.expire(key, time);
    }

    @Override
    public long getExpire(String key) {
        return redisService.getExpire(key);
    }

    @Override
    public boolean hasKey(String key) {
        return redisService.hasKey(key);
    }

    @Override
    public int del(String... key) {
        int count = redisService.del(key);
        try {
            cleanLocalCache(Arrays.asList(key).stream().map(k -> new CacheKey(k)).collect(Collectors.toList()).toArray(new CacheKey[0]));
        } catch (Exception e) {
            LOGGER.warn("(批量)删除缓存失败", e);
        }
        return count;
    }

    @Override
    public Object get(String key) {
        try {
            return localCache.get(new CacheKey(key)).getValue();
        } catch (Exception e) {
            LOGGER.warn("读缓存失败", e);
        }
        return null;
    }

    @Override
    public List<Object> mget(String... keys) {
        return redisService.mget(keys);
    }

    @Override
    public boolean set(String key, Object value) {
        boolean success = false;
        try {
            if (success = redisService.set(key, value)) {
                localCache.put(new CacheKey(key), new CacheValue(value));
            }
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean set(String key, Object value, long time) {
        boolean success = false;
        try {
            if (success = redisService.set(key, value, time)) {
                localCache.put(new CacheKey(key), new CacheValue(value));
            }
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public long incr(String key, long delta) {
        long result = -1;
        try {
            result = redisService.incr(key, delta);
            localCache.put(new CacheKey(key), new CacheValue(result));
        } catch (RedisException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("删除缓存失败", e);
        }
        return result;
    }

    @Override
    public long decr(String key, long delta) {
        long result = -1;
        try {
            result = redisService.decr(key, delta);
            localCache.put(new CacheKey(key), new CacheValue(result));
        } catch (RedisException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("删除缓存失败", e);
        }
        return result;
    }

    @Override
    public Object hget(String key, String item) {
        try {
            return localCache.get(new HashCacheKey(key, item)).getValue();
        } catch (Exception e) {
            LOGGER.warn("读缓存失败", e);
        }
        return null;
    }

    @Override
    public Map<Object, Object> hmget(String key) {
        try {
            return (Map<Object, Object>) localCache.get(new HashCacheKey(key)).getValue();
        } catch (Exception e) {
            LOGGER.warn("读缓存失败", e);
        }
        return null;
    }

    @Override
    public boolean hmset(String key, Map<String, Object> map) {
        boolean success = false;
        try {
            success = redisService.hmset(key, map);
            if (success) {
                CacheKey cacheKey = new HashCacheKey(key);
                cleanLocalCache(cacheKey);
                localCache.put(cacheKey, new CacheValue(map));
            }
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean hmset(String key, Map<String, Object> map, long time) {
        boolean success = false;
        try {
            success = redisService.hmset(key, map, time);
            if (success) {
                CacheKey cacheKey = new HashCacheKey(key);
                cleanLocalCache(cacheKey);
                localCache.put(cacheKey, new CacheValue(map));
            }
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean hset(String key, String item, Object value) {
        boolean success = false;
        try {
            success = redisService.hset(key, item, value);
            if (success) {
                HashCacheKey cacheKey = new HashCacheKey(key, item);
                cleanLocalCache(cacheKey);
                localCache.put(cacheKey, new CacheValue(value));
            }
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean hset(String key, String item, Object value, long time) {
        boolean success = false;
        try {
            success = redisService.hset(key, item, value, time);
            if (success) {
                HashCacheKey cacheKey = new HashCacheKey(key, item);
                cleanLocalCache(cacheKey);
                localCache.put(cacheKey, new CacheValue(value));
            }
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean hdel(String key, String... item) {
        boolean success = false;
        try {
            success = redisService.hdel(key, item);
            cleanLocalCache(new HashCacheKey(key)); // 删除整个hash缓存及其子项的本地副本
        } catch (Exception e) {
            LOGGER.warn("删除缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean hHasKey(String key, String item) {
        return redisService.hHasKey(key, item);
    }

    @Override
    public double hincr(String key, String item, double by) {
        double result = -1;
        try {
            result = redisService.hincr(key, item, by);
            localCache.invalidate(new HashCacheKey(key));
            localCache.put(new HashCacheKey(key, item), new CacheValue(result));
        } catch (RedisException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("删除缓存失败", e);
        }
        return result;
    }

    @Override
    public double hdecr(String key, String item, double by) {
        double result = -1;
        try {
            result = redisService.hdecr(key, item, by);
            localCache.invalidate(new HashCacheKey(key));
            localCache.put(new HashCacheKey(key, item), new CacheValue(result));
        } catch (RedisException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("删除缓存失败", e);
        }
        return result;
    }

    @Override
    public Set<Object> sGet(String key) {
        try {
            return (Set<Object>) localCache.get(new SetCacheKey(key)).getValue();
        } catch (Exception e) {
            LOGGER.warn("读取缓存失败");
        }
        return null;
    }

    @Override
    public boolean sHasValue(String key, Object value) {
        return redisService.sHasValue(key, value);
    }

    @Override
    public long sSet(String key, Object... values) {
        long result = 0;
        try {
            result = redisService.sSet(key, values);
            cleanLocalCache(new SetCacheKey(key));
        } catch (Exception e) {
            LOGGER.warn("更新缓存失败", e);
        }
        return result;
    }

    @Override
    public long sSetAndTime(String key, long time, Object... values) {
        long result = 0;
        try {
            result = redisService.sSet(key, time, values);
            cleanLocalCache(new SetCacheKey(key));
        } catch (Exception e) {
            LOGGER.warn("更新缓存失败", e);
        }
        return result;
    }

    @Override
    public long sGetSetSize(String key) {
        return redisService.sGetSetSize(key);
    }

    @Override
    public long setRemove(String key, Object... values) {
        long result = 0;
        try {
            result = redisService.setRemove(key, values);
            cleanLocalCache(new SetCacheKey(key));
        } catch (Exception e) {
            LOGGER.warn("清除缓存失败", e);
        }
        return result;
    }

    @Override
    public List<Object> lGet(String key, long start, long end) {
        return redisService.lGet(key, start, end);
    }

    @Override
    public long lGetListSize(String key) {
        return redisService.lGetListSize(key);
    }

    @Override
    public Object lGetIndex(String key, long index) {
        Object result = null;
        try {
            result = localCache.get(new ListCacheKey(key, index)).getValue();
        } catch (Exception e) {
            LOGGER.warn("读取缓存失败", e);
        }
        return result;
    }

    @Override
    public boolean lAdd(String key, Object value) {
        return lAdd(key, value, 0);
    }

    @Override
    public boolean lAdd(String key, Object value, long time) {
        boolean success = false;
        try {
            success = redisService.lAdd(key, value);
            cleanLocalCache(new ListCacheKey(key));
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean lAdd(String key, List<Object> value) {
        return lAdd(key, value, 0);
    }

    @Override
    public boolean lAdd(String key, List<Object> value, long time) {
        boolean success = false;
        try {
            success = redisService.lAdd(key, value);
            cleanLocalCache(new ListCacheKey(key));
        } catch (Exception e) {
            LOGGER.warn("写缓存失败", e);
        }
        return success;
    }

    @Override
    public boolean lUpdateIndex(String key, long index, Object value) {
        boolean success = false;
        try {
            success = redisService.lUpdateIndex(key, index, value);
            cleanLocalCache(new ListCacheKey(key, index));
        } catch (Exception e) {
            LOGGER.warn("更新缓存失败", e);
        }
        return success;
    }

    @Override
    public long lRemove(String key, long count, Object value) {
        long result = 0;
        try {
            result = redisService.lRemove(key, count, value);
            cleanLocalCache(new ListCacheKey(key));
        } catch (Exception e) {
            LOGGER.warn("清除缓存失败", e);
        }
        return result;
    }

    @Override
    public RedisService getKernel() {
        return redisService;
    }

    @Override
    public boolean setNX(String key, Object value) {
        return redisService.setNX(key, value);
    }

    @Override
    public RedisLocker buildLock(String key) {
        return redisService.buildLock(key);
    }

    @Override
    public RedisLocker buildLock(String key, long expire, TimeUnit timeUnit) {
        return redisService.buildLock(key, expire, timeUnit);
    }

    @Override
    public RedisTemplate getRedisTemplate() {
        return redisService.getRedisTemplate();
    }

    /**
     * 本地缓存副本清除
     *
     * @param cacheKeys
     */
    protected void cleanLocalCache(CacheKey... cacheKeys) {
        if (cacheKeys == null || cacheKeys.length == 0) {
            return;
        }
        try {
            for (CacheKey cacheKey : cacheKeys) {
                boolean isNest = cacheKey instanceof NestCacheKey;
                CacheKey parentKey = isNest ? new CacheKey(cacheKey.getKey()) : cacheKey;
                if (localCache.getIfPresent(parentKey) != null) { // 存在整体缓存
                    LOGGER.trace("移除本地缓存:{}", parentKey);
                    localCache.invalidate(parentKey); // 清除整体缓存
                }
                List<CacheKey> list = keyItemMap.get(cacheKey.getKey());
                if (!CollectionUtils.isEmpty(list)) { // 如果存在多个单元素缓存则一并清除
                    if (isNest && ((NestCacheKey) cacheKey).getItem() != null) {
                        LOGGER.trace("移除本地缓存子项:{}", cacheKey);
                        localCache.invalidate(cacheKey);
                    } else {
                        LOGGER.trace("批量移除本地缓存子项:{}", list);
                        localCache.invalidateAll(list);
                        list.clear();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("清除本地缓存失败", e);
            localCache.invalidateAll();
        }

    }

    /**
     * 缓存值对象，用于包装Redis远程缓存的返回结果
     *
     * @author 杨胜寒
     * @date 2018/9/17 下午4:14
     */
    static class CacheValue implements Serializable {

        private Object value;

        public CacheValue() {
        }

        public CacheValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    /**
     * 缓存key，覆写了hashCode和equals方法，仅供本地辅助缓存使用
     *
     * @author 杨胜寒
     * @date 2018/9/17 下午3:05
     */
    static class CacheKey implements Serializable {
        private String key;

        public CacheKey(String key) {
            if (StringUtils.isEmpty(key)) {
                throw new RedisException("请指定有效的缓存key");
            }
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey cacheKey = (CacheKey) o;
            return com.google.common.base.Objects.equal(toString(), cacheKey.toString());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(toString());
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

    /**
     * 嵌套类型缓存的key,如hash/set/list
     *
     * @param <T>
     */
    static class NestCacheKey<T> extends CacheKey {
        private T item;
        private String separater;

        public NestCacheKey(String key, String separater) {
            this(key, separater, null);
        }

        public NestCacheKey(String key, String separater, T item) {
            super(key);
            this.separater = separater;
            this.item = item;
        }

        public T getItem() {
            return item;
        }

        public String toString() {
            return StringUtils.isEmpty(item) ? super.getKey() : (super.getKey() + separater + item);
        }

    }

    /**
     * hash类型缓存的key
     */
    static class HashCacheKey extends NestCacheKey<String> {

        public HashCacheKey(String key) {
            super(key, null);
        }

        public HashCacheKey(String key, String item) {
            super(key, "_$_", item);
        }

    }

    /**
     * set类型缓存的key
     */
    static class SetCacheKey extends NestCacheKey<String> {

        public SetCacheKey(String key) {
            super(key, null);
        }

        public SetCacheKey(String key, String item) {
            super(key, "_._", item);
        }

    }

    /**
     * list类型缓存的key
     */
    static class ListCacheKey extends NestCacheKey<Long> {

        public ListCacheKey(String key) {
            super(key, null);
        }

        public ListCacheKey(String key, Long item) {
            super(key, "_:_", item);
        }

    }
}
