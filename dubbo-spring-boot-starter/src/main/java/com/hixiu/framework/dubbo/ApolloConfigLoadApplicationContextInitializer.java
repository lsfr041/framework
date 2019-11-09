/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: ApolloConfigLoadApplicationContextInitializer
 * Author:   程建乐
 * Date:     2019/9/1 0:30
 * Description: ApolloConfigLoadApplicationContextInitializer
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.dubbo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import com.ctrip.framework.foundation.Foundation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

/**
 * 〈一句话功能简述〉<br> 
 * 〈ApolloConfigLoadApplicationContextInitializer〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class ApolloConfigLoadApplicationContextInitializer  implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApolloApplicationContextInitializer.class);

    private final ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector.getInstance(ConfigPropertySourceFactory.class);

    private final static String CONFIG_NAMESPACE_PROPERTY = "dubbo.config.namespace";
    public static String CONFIG_NAMESPACE = "middleware.dubbo";

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        ConfigurableEnvironment environment = configurableApplicationContext.getEnvironment();
        String enabled = environment.getProperty("dubbo.apollo.disable", "false");
        if (Boolean.valueOf(enabled)) {
            LOGGER.info("Dubbo禁用Apollo远程配置");
            return;
        }

        String namespace = System.getProperty(CONFIG_NAMESPACE_PROPERTY, CONFIG_NAMESPACE);

        String beanName = "ApolloDubboPropertySources#" + namespace;
        if (environment.getPropertySources().contains(beanName)) {
            //already initialized
            return;
        }
        CompositePropertySource composite = new CompositePropertySource(beanName);
        LOGGER.info("从Apollo[{}]读取Dubbo配置", namespace);
        Config config = StringUtils.isEmpty(namespace) ? ConfigService.getAppConfig() : ConfigService.getConfig(namespace);
        if (StringUtils.isEmpty(config.getProperty("dubbo.application.name", ""))) { // 用户未设置dubbo.application.name时使用apollo app id
            String appId = Foundation.app().getAppId();
            if (StringUtils.isEmpty(appId)) {
                LOGGER.warn("未设置dubbo.application.name");
            } else {
                LOGGER.info("使用apollo app id作为dubbo.application.name:{}", appId);
                System.setProperty("dubbo.application.name", appId);
            }
        }
        LOGGER.info("注入来自Apollo的Dubbo配置");
        composite.addPropertySource(configPropertySourceFactory.getConfigPropertySource(namespace, config));
        environment.getPropertySources().addLast(composite); // 最低优先级,方便本地配置覆盖
    }
}
