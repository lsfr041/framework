/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: RedisConnectionConfiguration
 * Author:   程建乐
 * Date:     2019/9/1 0:43
 * Description: RedisConnectionConfiguration
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.redis.connection;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 〈一句话功能简述〉<br> 
 * 〈RedisConnectionConfiguration〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class RedisConnectionConfiguration {
    private final RedisProperties properties;
    private final RedisSentinelConfiguration sentinelConfiguration;
    private final RedisClusterConfiguration clusterConfiguration;

    protected RedisConnectionConfiguration(RedisProperties properties,
                                           RedisSentinelConfiguration sentinelConfiguration,
                                           RedisClusterConfiguration clusterConfiguration) {
        this.properties = properties;
        this.sentinelConfiguration = sentinelConfiguration;
        this.clusterConfiguration = clusterConfiguration;
    }

    public final RedisStandaloneConfiguration getStandaloneConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.hasText(this.properties.getUrl())) {
            RedisConnectionConfiguration.ConnectionInfo connectionInfo = parseUrl(this.properties.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(this.properties.getHost());
            config.setPort(this.properties.getPort());
            config.setPassword(RedisPassword.of(this.properties.getPassword()));
        }
        config.setDatabase(this.properties.getDatabase());
        return config;
    }

    /**
     * Create a {@link RedisClusterConfiguration} if necessary.
     *
     * @return {@literal null} if no cluster settings are set.
     */
    protected final RedisClusterConfiguration getClusterConfiguration() {
        if (this.clusterConfiguration != null) {
            return this.clusterConfiguration;
        }
        if (this.properties.getCluster() == null) {
            return null;
        }
        RedisProperties.Cluster clusterProperties = this.properties.getCluster();
        RedisClusterConfiguration config = new RedisClusterConfiguration(
                clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        if (this.properties.getPassword() != null) {
            config.setPassword(RedisPassword.of(this.properties.getPassword()));
        }
        return config;
    }

    protected RedisConnectionConfiguration.ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            boolean useSsl = (url.startsWith("rediss://"));
            String password = null;
            if (uri.getUserInfo() != null) {
                password = uri.getUserInfo();
                int index = password.indexOf(':');
                if (index >= 0) {
                    password = password.substring(index + 1);
                }
            }
            return new RedisConnectionConfiguration.ConnectionInfo(uri, useSsl, password);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Malformed url '" + url + "'", ex);
        }
    }

    protected static class ConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String password;

        public ConnectionInfo(URI uri, boolean useSsl, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.password = password;
        }

        public boolean isUseSsl() {
            return this.useSsl;
        }

        public String getHostName() {
            return this.uri.getHost();
        }

        public int getPort() {
            return this.uri.getPort();
        }

        public String getPassword() {
            return this.password;
        }

    }
}
