/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AutowiredAnnotationConfig
 * Author:   程建乐
 * Date:     2019/8/31 23:07
 * Description: AutowiredAnnotationConfig
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

import java.io.Serializable;
import java.util.StringJoiner;

/**
 * 〈一句话功能简述〉<br> 
 * 〈AutowiredAnnotationConfig〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public class AutowiredAnnotationConfig implements Serializable {

    private String value;

    private boolean primary;

    public AutowiredAnnotationConfig() {
    }

    public AutowiredAnnotationConfig(String value) {
        this.value = value;
    }

    public AutowiredAnnotationConfig(String value, boolean primary) {
        this.value = value;
        this.primary = primary;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("value='" + value + "'")
                .add("primary=" + primary)
                .toString();
    }

}
