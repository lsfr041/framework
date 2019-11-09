/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AutowiredFieldElement
 * Author:   程建乐
 * Date:     2019/8/31 22:59
 * Description: AutowiredFieldElement
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * 〈一句话功能简述〉<br> 
 * 〈AutowiredFieldElement〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
class AutowiredFieldElement<A> extends InjectionMetadata.InjectedElement {
    private final Field field;
    private final A autowired;
    private final AutowiredBeanBuilder<A> builder;

    AutowiredFieldElement(Field field, A autowired, AutowiredBeanBuilder builder) {
        super(field, null);
        this.field = field;
        this.autowired = autowired;
        this.builder = builder;
    }

    @Override
    protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
        Class<?> autowiredType = field.getType();
        Object autowiredBean = builder.build(autowired, autowiredType);
        ReflectionUtils.makeAccessible(field);
        field.set(bean, autowiredBean);
    }
}
