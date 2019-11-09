/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisServiceImpl
 * Author:   程建乐
 * Date:     2019/9/1 0:54
 * Description:
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisServiceImpl implements RedisService {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisServiceImpl.class);

    private static final RedisScript<String> SCRIPT_LOCK = new DefaultRedisScript<>("return redis.call('set',KEYS[1],ARGV[1],'NX','PX',ARGV[2])", String.class);
    private static final RedisScript<String> SCRIPT_UNLOCK = new DefaultRedisScript<>("if redis.call('get',KEYS[1]) == ARGV[1] then return tostring(redis.call('del',KEYS[1])==1) else return 'false' end", String.class);
    private static final String LOCK_SUCCESS = "OK";

    private HixiuRedisProperties config;
    private RedisTemplate<String, Object> redisTemplate;

    public RedisServiceImpl(HixiuRedisProperties config, RedisTemplate<String, Object> redisTemplate) {
        if (redisTemplate == null) {
            throw new RedisException("请指定RedisTemplate");
        }
        this.config = config == null ? new HixiuRedisProperties() : config;
        this.redisTemplate = redisTemplate;
        LOGGER.info("Redis命名空间:{}，默认过期时间:{}秒", config.getNamespace(), config.getDefaultExpire());
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(buildKey(key), time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("设置缓存过期时间失败", e);
            return false;
        }
    }

    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(buildKey(key), TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RedisException("获取缓存过期时间失败", e);
        }
    }

    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(buildKey(key));
        } catch (Exception e) {
            LOGGER.error("检查缓存key是否存在失败", e);
            return false;
        }
    }

    public int del(String... key) {
        int count = -1;
        if (key != null && key.length > 0) {
            try {
                redisTemplate.delete(Arrays.asList(key).stream().map(item -> buildKey(item)).collect(Collectors.toList()));
                count = key.length;
            } catch (Exception e) {
                LOGGER.error("(批量)删除缓存失败", e);
            }
        }
        return count;
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(buildKey(key));
        } catch (Exception e) {
            LOGGER.error("获取缓存失败", e);
        }
        return null;
    }

    public List<Object> mget(String... keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }
        try {
            List<String> set = new LinkedList<>();
            for (String key : keys) {
                if (StringUtils.isEmpty(key)) {
                    continue;
                }
                set.add(buildKey(key));
            }
            if (set.isEmpty()) {
                return null;
            }
            return redisTemplate.opsForValue().multiGet(set);
        } catch (Exception e) {
            LOGGER.error("批量获取缓存失败", e);
        }
        return null;
    }

    public boolean set(String key, Object value) {
        return set(key, value, config.getDefaultExpire());
    }

    public boolean set(String key, Object value, long time) {
        try {
            key = buildKey(key);
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("设置缓存失败", e);
            return false;
        }
    }

    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RedisException("递增因子必须大于0");
        }
        try {
            return redisTemplate.opsForValue().increment(buildKey(key), delta);
        } catch (Exception e) {
            throw new RedisException("缓存递增失败", e);
        }
    }

    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RedisException("递减因子必须大于0");
        }
        try {
            return redisTemplate.opsForValue().increment(buildKey(key), -delta);
        } catch (Exception e) {
            throw new RedisException("缓存递减失败", e);
        }
    }

    public Object hget(String key, String item) {
        try {
            return redisTemplate.opsForHash().get(buildKey(key), item);
        } catch (Exception e) {
            LOGGER.error("获取Hash类型缓存的某个子项失败", e);
        }
        return null;
    }

    public Map<Object, Object> hmget(String key) {
        try {
            return redisTemplate.opsForHash().entries(buildKey(key));
        } catch (Exception e) {
            LOGGER.error("获取Hash类型缓存所有子项失败", e);
        }
        return null;
    }

    public boolean hmset(String key, Map<String, Object> map) {
        return hmset(key, map, config.getDefaultExpire());
    }

    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForHash().putAll(buildKey(key), map);
                    expire(key, time);
                    operations.exec();
                    return true;
                }
            });
        } catch (Exception e) {
            LOGGER.error("设置Hash类型缓存值失败", e);
            return false;
        }
    }

    public boolean hset(String key, String item, Object value) {
        return hset(key, item, value, config.getDefaultExpire());
    }

    public boolean hset(String key, String item, Object value, long time) {
        try {
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForHash().put(buildKey(key), item, value);
                    expire(key, time);
                    operations.exec();
                    return true;
                }
            });
        } catch (Exception e) {
            LOGGER.error("设置hash类型缓存某个子项的值失败", e);
            return false;
        }
    }

    public boolean hdel(String key, String... item) {
        if (item != null && item.length > 0) {
            try {
                redisTemplate.opsForHash().delete(buildKey(key), item);
                return true;
            } catch (Exception e) {
                LOGGER.error("(批量)删除hash类型缓存子项失败", e);
            }
        }
        return false;
    }

    public boolean hHasKey(String key, String item) {
        try {
            return redisTemplate.opsForHash().hasKey(buildKey(key), item);
        } catch (Exception e) {
            LOGGER.error("检查hash类型缓存是否存在指定子项失败", e);
        }
        return false;
    }

    public double hincr(String key, String item, double by) {
        try {
            return redisTemplate.opsForHash().increment(buildKey(key), item, by);
        } catch (Exception e) {
            throw new RedisException("Hash类型缓存递增失败", e);
        }

    }

    public double hdecr(String key, String item, double by) {
        try {
            return redisTemplate.opsForHash().increment(buildKey(key), item, -by);
        } catch (Exception e) {
            throw new RedisException("Hash类型缓存递减失败", e);
        }
    }

    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(buildKey(key));
        } catch (Exception e) {
            LOGGER.error("获取set类型缓存值失败", e);
            return null;
        }
    }

    public boolean sHasValue(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(buildKey(key), value);
        } catch (Exception e) {
            LOGGER.error("移除set类型缓存指定元素失败", e);
            return false;
        }
    }

    public long sSet(String key, Object... values) {
        return sSetAndTime(key, config.getDefaultExpire(), values);
    }

    public long sSetAndTime(String key, long time, Object... values) {
        try {
            return redisTemplate.execute(new SessionCallback<Integer>() {
                @Override
                public Integer execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForSet().add(buildKey(key), values);
                    expire(key, time);
                    operations.exec();
                    return values.length;
                }
            });
        } catch (Exception e) {
            LOGGER.error("设置set类型缓存值失败", e);
            return -1;
        }
    }

    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(buildKey(key));
        } catch (Exception e) {
            LOGGER.error("获取set类型缓存元素数量失败", e);
            return -1;
        }
    }

    public long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(buildKey(key), values);
        } catch (Exception e) {
            LOGGER.error("移除set类型缓存指定元素失败", e);
            return -1;
        }
    }

    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(buildKey(key), start, end);
        } catch (Exception e) {
            LOGGER.error("获取list类型缓存指定区间元素失败", e);
            return null;
        }
    }

    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(buildKey(key));
        } catch (Exception e) {
            LOGGER.error("获取List类型缓存元素数量失败", e);
            return -1;
        }
    }

    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(buildKey(key), index);
        } catch (Exception e) {
            LOGGER.error("获取List类型缓存某个元素失败", e);
            return null;
        }
    }

    public boolean lAdd(String key, Object value) {
        return lAdd(key, value, config.getDefaultExpire());
    }

    public boolean lAdd(String key, Object value, long time) {
        try {
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForList().rightPush(buildKey(key), value);
                    expire(key, time);
                    operations.exec();
                    return true;
                }
            });
        } catch (Exception e) {
            LOGGER.error("在List类型缓存后追加元素失败", e);
            return false;
        }
    }

    public boolean lAdd(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(buildKey(key), value);
            return true;
        } catch (Exception e) {
            LOGGER.error("设置List类型缓存值失败", e);
            return false;
        }
    }

    public boolean lAdd(String key, List<Object> value, long time) {
        try {
            return redisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForList().rightPushAll(buildKey(key), value);
                    expire(key, time);
                    operations.exec();
                    return true;
                }
            });
        } catch (Exception e) {
            LOGGER.error("设置List类型缓存值失败", e);
            return false;
        }
    }

    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(buildKey(key), index, value);
            return true;
        } catch (Exception e) {
            LOGGER.error("设置List类型缓存某个子项的值失败", e);
            return false;
        }
    }

    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(buildKey(key), count, value);
        } catch (Exception e) {
            LOGGER.error("移除List类型缓存指定头部元素失败", e);
            return -1;
        }

    }

    @Override
    public RedisService getKernel() {
        return this;
    }

    private boolean lock() {
        return false;
    }


    @Override
    public boolean setNX(String key, Object value) {
        try {
            return redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.setNX(new StringRedisSerializer().serialize(key), new GenericJackson2JsonRedisSerializer().serialize(value)));
        } catch (Exception e) {
            LOGGER.error("setNX操作失败", e);
        }
        return false;
    }

    @Override
    public RedisLocker buildLock(String key) {
        return buildLock(key, config.getDefaultLockExpire(), TimeUnit.SECONDS);
    }

    @Override
    public RedisLocker buildLock(String key, long expire, TimeUnit timeUnit) {
        return new RedisLockImpl(buildKey(key), expire, timeUnit);
    }

    @Override
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    protected String buildKey(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new RedisException("Redis缓存key为空");
        }
        return StringUtils.isEmpty(this.config.getNamespace()) ? key : String.format("%s_%s", this.config.getNamespace(), key);
    }

    /**
     * 基于Redis的锁对象
     */
    private class RedisLockImpl implements RedisLocker {

        private String key;
        private long expire;
        private String value;

        protected RedisLockImpl(String key, long expire, TimeUnit timeUnit) {
            this.key = key;
            this.expire = timeUnit.toMillis(expire);
            this.value = UUID.randomUUID().toString();
        }

        @Override
        public boolean tryLock() {
            String result = redisTemplate.execute(SCRIPT_LOCK,
                    redisTemplate.getStringSerializer(),
                    redisTemplate.getStringSerializer(),
                    Collections.singletonList(key),
                    value,
                    String.valueOf(expire));
            boolean success = LOCK_SUCCESS.equalsIgnoreCase(result);
            LOGGER.debug("获取 Redis 锁, result: {}, key: {}, value: {}, expire: {}", success, key, value, expire);
            return success;

        }

        @Override
        public synchronized void release() {
            LOGGER.debug("释放 Redis 锁: key: {}, value: {}, expire: {}", key, value, expire);
            redisTemplate.execute(SCRIPT_UNLOCK,
                    redisTemplate.getStringSerializer(),
                    redisTemplate.getStringSerializer(),
                    Collections.singletonList(key),
                    value);

        }

        @Override
        public Object getKey() {
            return key;
        }

    }
}
