/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: ConfigurationPropertiesBinder
 * Author:   程建乐
 * Date:     2019/8/31 22:52
 * Description: confog
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.properties;

import com.ctrip.framework.apollo.spring.config.ConfigPropertySource;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.IgnoreTopLevelConverterNotFoundBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.bind.validation.ValidationBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 〈一句话功能简述〉<br> 
 * 〈config〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public class ConfigurationPropertiesBinder {
    private final DefaultListableBeanFactory beanFactory;
    private final PropertySource propertySource;
    private volatile Binder binder;

    public ConfigurationPropertiesBinder(DefaultListableBeanFactory beanFactory, ConfigPropertySource propertySource) {
        this.beanFactory = beanFactory;
        this.propertySource = propertySource;
    }

    public void bind(Bindable<?> target) {
        ConfigurationProperties annotation = target.getAnnotation(ConfigurationProperties.class);
        Assert.state(annotation != null, () -> "Missing @ConfigurationProperties on " + target);
        List<Validator> validators = getValidators(target);
        BindHandler bindHandler = getBindHandler(annotation, validators);
        getBinder().bind(annotation.prefix(), target, bindHandler);
    }

    private List<Validator> getValidators(Bindable<?> target) {
        List<Validator> validators = new ArrayList<>(3);
        if (target.getValue() != null && target.getValue().get() instanceof Validator) {
            validators.add((Validator) target.getValue().get());
        }
        return validators;
    }

    private BindHandler getBindHandler(ConfigurationProperties annotation, List<Validator> validators) {
        BindHandler handler = new IgnoreTopLevelConverterNotFoundBindHandler();
        if (annotation.ignoreInvalidFields()) {
            handler = new IgnoreErrorsBindHandler(handler);
        }
        if (!annotation.ignoreUnknownFields()) {
            UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
            handler = new NoUnboundElementsBindHandler(handler, filter);
        }
        if (!validators.isEmpty()) {
            handler = new ValidationBindHandler(handler, validators.toArray(new Validator[0]));
        }
        return handler;
    }

    private Binder getBinder() {
        if (this.binder == null) {
            this.binder = new Binder(getConfigurationPropertySources(), getPropertySourcesPlaceholdersResolver(), getConversionService(), getPropertyEditorInitializer());
        }
        return this.binder;
    }

    private Iterable<ConfigurationPropertySource> getConfigurationPropertySources() {
        return ConfigurationPropertySources.from(this.propertySource);
    }

    private PropertySourcesPlaceholdersResolver getPropertySourcesPlaceholdersResolver() {
        return new PropertySourcesPlaceholdersResolver(Arrays.asList(this.propertySource));
    }

    private ConversionService getConversionService() {
        return new ConversionServiceDeducer(this.beanFactory).getConversionService();
    }

    private Consumer<PropertyEditorRegistry> getPropertyEditorInitializer() {
        return beanFactory::copyRegisteredEditorsTo;
    }
}
