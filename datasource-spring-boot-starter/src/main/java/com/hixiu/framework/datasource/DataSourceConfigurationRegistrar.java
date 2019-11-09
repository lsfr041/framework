/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: DataSourceConfigurationRegistrar
 * Author:   程建乐
 * Date:     2019/9/1 0:15
 * Description: DataSourceConfigurationRegistrar
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.datasource;

import com.hixiu.framework.common.inject.AbstractConfigurationRegistrar;
import com.hixiu.framework.common.inject.AutowiredAnnotationConfig;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 〈一句话功能简述〉<br> 
 * 〈DataSourceConfigurationRegistrar〉
 *
 * @author 程建乐
 * @create 2019/9/1
 * @since 1.0.0
 */
public class DataSourceConfigurationRegistrar  extends AbstractConfigurationRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AutowiredAnnotationConfig[] configs = parseAutowiredAnnotationConfigs(metadata, EnableDataSourceConfiguration.class.getName());
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) registry;
        Object processor = new DataSourceAutowiredAnnotationProcessor(beanFactory, configs);
        beanFactory.registerSingleton(DataSourceAutowiredAnnotationProcessor.CLASS_NAME, processor);
    }
}
