/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: EnhancedJedisPool
 * Author:   程建乐
 * Date:     2019/9/1 1:01
 * Description:
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package org.springframework.data.redis.connection.jedis;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 〈支持废弃连接自动清理的JedisPool〉<br>
 * 〈〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class EnhancedJedisPool extends JedisPool {
    private final static Logger LOGGER = LoggerFactory.getLogger(EnhancedJedisPool.class);

    public EnhancedJedisPool(GenericObjectPoolConfig poolConfig, String host, int port, int connectionTimeout, int soTimeout,
                             String password, int database, String clientName, JedisClientConfiguration clientConfiguration) {
        super(poolConfig, host, port, connectionTimeout, soTimeout, password, database, clientName, clientConfiguration.isUseSsl(),
                clientConfiguration.getSslSocketFactory().orElse(null), //
                clientConfiguration.getSslParameters().orElse(null), //
                clientConfiguration.getHostnameVerifier().orElse(null));
    }

    @Override
    public void initPool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<Jedis> factory) {
        super.initPool(poolConfig, factory);
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        abandonedConfig.setLogAbandoned(true);
        abandonedConfig.setLogWriter(new PrintWriter(new StringWriter() {
            @Override
            public void flush() {
                super.flush();
                String content = this.getBuffer().toString();
                if (content != null && content.length() > 0) {
                    LOGGER.warn("Redis废弃连接清理:" + content);
                    this.getBuffer().delete(0, content.length());
                }
            }
        }));
        abandonedConfig.setRemoveAbandonedOnBorrow(true);
        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
        abandonedConfig.setRemoveAbandonedTimeout(120); // 120s未释放的连接强制销毁
        abandonedConfig.setUseUsageTracking(true);
        abandonedConfig.setRequireFullStackTrace(true);
        LOGGER.info("设置Redis废弃连接清理策略:{}", abandonedConfig);
        internalPool.setAbandonedConfig(abandonedConfig); // 设置废弃连接处理配置
    }
}
