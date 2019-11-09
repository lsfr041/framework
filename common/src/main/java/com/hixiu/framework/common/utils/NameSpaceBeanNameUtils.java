/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: NameSpaceBeanNameUtils
 * Author:   程建乐
 * Date:     2019/8/31 22:58
 * Description: NameSpaceBeanNameUtils
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.utils;

/**
 * 〈一句话功能简述〉<br> 
 * 〈NameSpaceBeanNameUtils〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public class NameSpaceBeanNameUtils {

    private final static String BEAN_NAME_SEPARATOR = "#";

    /**
     * 生成namespace bean name
     *
     * @param type
     * @param namespace
     * @return
     */
    public static String build(Class<?> type, String namespace) {
        return type.getName() + BEAN_NAME_SEPARATOR + namespace;
    }
}
