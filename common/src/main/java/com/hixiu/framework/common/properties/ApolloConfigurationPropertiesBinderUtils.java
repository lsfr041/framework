/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: ApolloConfigurationPropertiesBinderUtils
 * Author:   程建乐
 * Date:     2019/8/31 22:51
 * Description: Apollo配置中心获取属性工具类
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.properties;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import com.ctrip.framework.apollo.spring.config.ConfigPropertySourceFactory;
import com.ctrip.framework.apollo.spring.util.SpringInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;

/**
 * 〈一句话功能简述〉<br>
 * 〈Apollo配置中心获取属性工具类〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public class ApolloConfigurationPropertiesBinderUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(ApolloConfigurationPropertiesBinderUtils.class);

    public static <T> T bindFromNamespace(DefaultListableBeanFactory beanFactory, String namespace, T bean) {

        LOGGER.debug("从[{}]读取配置[{}]", namespace, bean.getClass().getName());
        Config config = StringUtils.isEmpty(namespace) ? ConfigService.getAppConfig() : ConfigService.getConfig(namespace);
        ConfigPropertySourceFactory configPropertySourceFactory = SpringInjector.getInstance(ConfigPropertySourceFactory.class);
        ConfigPropertySource source = configPropertySourceFactory.getConfigPropertySource(namespace, config);
        Annotation annotation = AnnotationUtils.findAnnotation(bean.getClass(), ConfigurationProperties.class);
        ResolvableType type = ResolvableType.forClass(bean.getClass());
        Bindable<?> target = Bindable.of(type).withExistingValue(bean).withAnnotations(annotation);
        new ConfigurationPropertiesBinder(beanFactory, source).bind(target);
        return bean;

    }
}
