/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AbstractAutowiredBeanRegistrar
 * Author:   程建乐
 * Date:     2019/8/31 23:07
 * Description: AbstractAutowiredBeanRegistrar
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.Assert;

/**
 * 〈一句话功能简述〉<br> 
 * 〈AbstractAutowiredBeanRegistrar〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public abstract class AbstractAutowiredBeanRegistrar<T> {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected DefaultListableBeanFactory beanFactory;
    protected AutowiredAnnotationConfig config;
    protected T autowiredBean;

    public AbstractAutowiredBeanRegistrar(AutowiredAnnotationConfig config, DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.config = config;
        Assert.hasLength(config.getValue(), "请指定配置所在的Namespace");
    }

    protected void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinition.setPrimary(config.isPrimary());
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
        if (config.isPrimary()) {
            logger.info("根据配置将当前Bean暴露为Primary:{}", beanName);
        }
    }

    protected void registerSingleton(String beanName, Object bean) {
        beanFactory.registerSingleton(beanName, bean);
    }

    public T register() {
        if (beanFactory.containsBean(this.config.getValue())) {
            logger.debug("AutowiredBean已存在,直接返回:{}", this.config.getValue());
            this.autowiredBean = (T) beanFactory.getBean(this.config.getValue());
        } else {
            logger.info("AutowiredBean不存在,即刻创建:{}", this.config);
            this.autowiredBean = build();
            registerSingleton(config.getValue(), autowiredBean);
        }
        return autowiredBean;
    }

    protected abstract T build();
}
