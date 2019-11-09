/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: LimitJackson2JsonRedisSerializer
 * Author:   程建乐
 * Date:     2019/9/1 0:46
 * Description: LimitJackson2JsonRedisSerializer
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 *  限制长度的redis value serializer <br>
 * 〈LimitJackson2JsonRedisSerializer〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class LimitJackson2JsonRedisSerializer  extends GenericJackson2JsonRedisSerializer {

    private final static long DEFAULT_LIMIT = 1024 * 512;
    private long limit;

    public LimitJackson2JsonRedisSerializer() {
        this(DEFAULT_LIMIT);
    }

    public LimitJackson2JsonRedisSerializer(long limit) {
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    @Override
    public byte[] serialize(Object source) throws SerializationException {
        byte[] result = super.serialize(source);
        if (result != null && limit > 0 && result.length > limit) {
            throw new RedisException(String.format("Redis缓存值超出限制(%d>%d)", result.length, limit));
        }
        return result;
    }
}
