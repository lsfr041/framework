/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AutowiredBean
 * Author:   程建乐
 * Date:     2019/8/31 22:59
 * Description: AutowiredBean
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

import org.springframework.beans.factory.DisposableBean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈AutowiredBean〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public abstract class AutowiredBean<T> implements DisposableBean {

    private String namespace;

    private T properties;

    public AutowiredBean(String namespace, T properties) {
        this.namespace = namespace;
        this.properties = properties;
    }

    public String getNamespace() {
        return namespace;
    }

    public T getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return getClass().getName() + "#" + namespace;
    }
}
