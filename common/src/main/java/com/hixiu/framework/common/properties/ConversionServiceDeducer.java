/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: ConversionServiceDeducer
 * Author:   程建乐
 * Date:     2019/8/31 22:53
 * Description: de
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.properties;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Collections;
import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈de〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public class ConversionServiceDeducer {

    private final DefaultListableBeanFactory beanFactory;

    ConversionServiceDeducer(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public ConversionService getConversionService() {
        try {
            return this.beanFactory.getBean(ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
        } catch (NoSuchBeanDefinitionException ex) {
            return this.beanFactory.createBean(ConversionServiceDeducer.Factory.class).create();
        }
    }

    private static class Factory {

        private List<Converter<?, ?>> converters = Collections.emptyList();

        private List<GenericConverter> genericConverters = Collections.emptyList();

        /**
         * A list of custom converters (in addition to the defaults) to use when
         * converting properties for binding.
         *
         * @param converters the converters to set
         */
        @Autowired(required = false)
        @ConfigurationPropertiesBinding
        public void setConverters(List<Converter<?, ?>> converters) {
            this.converters = converters;
        }

        /**
         * A list of custom converters (in addition to the defaults) to use when
         * converting properties for binding.
         *
         * @param converters the converters to set
         */
        @Autowired(required = false)
        @ConfigurationPropertiesBinding
        public void setGenericConverters(List<GenericConverter> converters) {
            this.genericConverters = converters;
        }

        public ConversionService create() {
            if (this.converters.isEmpty() && this.genericConverters.isEmpty()) {
                return ApplicationConversionService.getSharedInstance();
            }
            ApplicationConversionService conversionService = new ApplicationConversionService();
            for (Converter<?, ?> converter : this.converters) {
                conversionService.addConverter(converter);
            }
            for (GenericConverter genericConverter : this.genericConverters) {
                conversionService.addConverter(genericConverter);
            }
            return conversionService;
        }

    }
}
