/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AutowiredAnnotationProcessor
 * Author:   程建乐
 * Date:     2019/8/31 23:05
 * Description: AutowiredAnnotationProcessor
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * 〈一句话功能简述〉<br> 
 * 〈AutowiredAnnotationProcessor〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public abstract class AutowiredAnnotationProcessor<B extends AutowiredBean, A extends Annotation> extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware, DisposableBean, AutowiredBeanBuilder<A> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AutowiredAnnotationProcessor.class);


    protected final Class<A> annotationType;
    protected DefaultListableBeanFactory beanFactory;
    protected final ConcurrentMap<String, B> autowiredBeansCache = new ConcurrentHashMap<>();
    protected final ConcurrentMap<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    public AutowiredAnnotationProcessor(Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    public AutowiredAnnotationProcessor(Class<A> annotationType, DefaultListableBeanFactory beanFactory, AutowiredAnnotationConfig... configs) {
        this.annotationType = annotationType;
        setBeanFactory(beanFactory);
        if (configs != null && configs.length > 0) {
            for (AutowiredAnnotationConfig config : configs) {
                build(config, null);
            }
        }
    }

    @Override
    public Object build(A autowired, Class<?> autowiredType) {
        AutowiredAnnotationConfig config = parseConfig(autowired);
        Assert.hasLength(config.getValue(), "请指定配置信息所在的Namespace");
        return build(config, autowiredType);
    }


    /**
     * 从指定namespace下读取配置并创建所需的bean,如果bean已经存在则直接返回已存在的bean
     *
     * @param config
     * @param autowiredType
     * @return
     */
    protected Object build(AutowiredAnnotationConfig config, Class<?> autowiredType) {
        B autowiredBean = autowiredBeansCache.get(config.getValue());
        if (autowiredBean == null) {
            LOGGER.debug("读取Namespace=[{}]下的配置并创建AutowiredBean . . .", config.getValue());
            autowiredBean = create(config);
            autowiredBeansCache.putIfAbsent(config.getValue(), autowiredBean);
        }
        Object targetBean = null;
        if (autowiredType != null) {
            LOGGER.debug("自动注入:NameSpace=[{}] , Bean=[{}]", config.getValue(), autowiredType);
            targetBean = getBean(autowiredBean, autowiredType);
            if (targetBean == null) {
                throw new NoSuchBeanDefinitionException(autowiredType, "不支持的注入类型");
            }
        }
        return targetBean;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findAutowiredMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "根据自定义注解注入bean失败", ex);
        }
        return pvs;
    }

    private InjectionMetadata findAutowiredMetadata(String beanName, Class<?> clazz, PropertyValues pvs){
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildAutowiredMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() + "] for autowired metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildAutowiredMetadata(final Class<?> beanClass) {
        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<>();
        elements.addAll(findFieldAutowiredMetadata(beanClass));
        elements.addAll(findMethodAutowiredMetadata(beanClass));
        return new InjectionMetadata(beanClass, elements);

    }


    private List<InjectionMetadata.InjectedElement> findMethodAutowiredMetadata(final Class<?> beanClass) {
        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<>();
        ReflectionUtils.doWithMethods(beanClass, method -> {
            Method bridgedMethod = findBridgedMethod(method);
            if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                return;
            }
            A autowired = findAnnotation(bridgedMethod, annotationType);
            if (autowired != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                if (Modifier.isStatic(method.getModifiers())) {
                    LOGGER.warn("@{} annotation is not supported on static methods: {}", annotationType, method);
                    return;
                }
                if (method.getParameterTypes().length == 0) {
                    LOGGER.warn("@{}  annotation should only be used on methods with parameters: {}", annotationType, method);
                }
                PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                elements.add(new AutowiredMethodElement<>(method, pd, autowired, this));
            }
        });
        return elements;
    }

    private List<InjectionMetadata.InjectedElement> findFieldAutowiredMetadata(final Class<?> beanClass) {
        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<>();
        ReflectionUtils.doWithFields(beanClass, field -> {
            A autowired = getAnnotation(field, annotationType);
            if (autowired != null) {
                if (Modifier.isStatic(field.getModifiers())) {
                    LOGGER.warn("@{} annotation is not supported on static fields: {}", annotationType, field);
                    return;
                }
                elements.add(new AutowiredFieldElement<>(field, autowired, this));
            }
        });
        return elements;
    }

    @Override
    public void destroy() throws Exception {
        for (B bean : autowiredBeansCache.values()) {
            LOGGER.debug("Shutting Down : {}", bean);
            try {
                bean.destroy();
            } catch (Exception e) {
                LOGGER.warn("Destroy Bean Failed !", e);
            }
        }
        injectionMetadataCache.clear();
        autowiredBeansCache.clear();
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findAutowiredMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 2;
    }

    /**
     * 根据指定配置创建所需的bean
     *
     * @param config
     * @return
     */
    protected abstract B create(AutowiredAnnotationConfig config);

    /**
     * 从指定的AutowiredBean中获取指定类型的属性bean,用于自动装配
     *
     * @param autowiredBean
     * @param autowiredType
     * @return
     */
    protected abstract Object getBean(B autowiredBean, Class<?> autowiredType);


    /**
     * 从注解中获取解析自动装入配置
     *
     * @param autowired
     * @return
     */
    protected abstract AutowiredAnnotationConfig parseConfig(A autowired);


}
