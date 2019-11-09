/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: HixiuRedisProperties
 * Author:   程建乐
 * Date:     2019/9/1 0:55
 * Description:
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class HixiuRedisProperties extends RedisProperties implements Serializable {
    /**
     * 缓存命名空间
     */
    private String namespace;
    /**
     * 默认过期时间(S)，默认一天
     */
    private long defaultExpire = 86400;
    /**
     * redis锁的默认过期时间(S)
     */
    private long defaultLockExpire = 60;
    /**
     * redis value长度限制(byte)
     */
    private long valueLimitBytes;
    /**
     * 本地辅助缓存
     */
    private LocalCache localCache = new LocalCache();
    /**
     * 是否支持事务
     */
    private boolean enableTransactionSupport = false;
    /**
     * 是否支持将连接池暴露到jmx
     */
    private boolean enableExportJmx = true;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public long getDefaultExpire() {
        return defaultExpire;
    }

    public void setDefaultExpire(long defaultExpire) {
        this.defaultExpire = defaultExpire;
    }

    public long getDefaultLockExpire() {
        return defaultLockExpire;
    }

    public void setDefaultLockExpire(long defaultLockExpire) {
        this.defaultLockExpire = defaultLockExpire;
    }

    public long getValueLimitBytes() {
        return valueLimitBytes;
    }

    public void setValueLimitBytes(long valueLimitBytes) {
        this.valueLimitBytes = valueLimitBytes;
    }

    public LocalCache getLocalCache() {
        return localCache;
    }

    public void setLocalCache(LocalCache localCache) {
        this.localCache = localCache;
    }

    public boolean isEnableTransactionSupport() {
        return enableTransactionSupport;
    }

    public void setEnableTransactionSupport(boolean enableTransactionSupport) {
        this.enableTransactionSupport = enableTransactionSupport;
    }

    public boolean isEnableExportJmx() {
        return enableExportJmx;
    }

    public void setEnableExportJmx(boolean enableExportJmx) {
        this.enableExportJmx = enableExportJmx;
    }

    public static class LocalCache implements Serializable {
        /**
         * 是否开启本地辅助缓存
         */
        private boolean enable = true;
        /**
         * 本地辅助缓存最大元素数量
         */
        private int maxSize = 500;
        /**
         * 缓存写入后的默认过期时间(ms)
         */
        private long expireAfterWrite = 100;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public long getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public void setExpireAfterWrite(long expireAfterWrite) {
            this.expireAfterWrite = expireAfterWrite;
        }

        @Override
        public String toString() {
            return String.format("Enable=%s, MaxSize=%d, ExpireAfterWrite=%d", enable, maxSize, expireAfterWrite);
        }
    }
}
