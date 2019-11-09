/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AutowiredMethodElement
 * Author:   程建乐
 * Date:     2019/8/31 23:10
 * Description: AutowiredMethodElement
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * 〈一句话功能简述〉<br>
 * 〈AutowiredMethodElement〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
class AutowiredMethodElement<A> extends InjectionMetadata.InjectedElement {

    private final Method method;
    private final A autowired;
    private final AutowiredBeanBuilder<A> builder;

    AutowiredMethodElement(Method method, PropertyDescriptor pd, A autowired, AutowiredBeanBuilder<A> builder) {
        super(method, pd);
        this.method = method;
        this.autowired = autowired;
        this.builder = builder;
    }

    @Override
    protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
        Class<?> autowiredClass = pd.getPropertyType();
        Object autowiredBean = builder.build(autowired, autowiredClass);
        ReflectionUtils.makeAccessible(method);
        method.invoke(bean, autowiredBean);
    }
}
