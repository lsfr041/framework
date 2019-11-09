/**
 * Copyright (C), 2018-2019, 个人有限公司
 * FileName: AutowiredBeanBuilder
 * Author:   程建乐
 * Date:     2019/8/31 23:00
 * Description: AutowiredBeanBuilder
 * Emaill:  jianle8858@163.com
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.hixiu.framework.common.inject;

/**
 * 〈一句话功能简述〉<br> 
 * 〈AutowiredBeanBuilder〉
 *
 * @author 程建乐
 * @create 2019/8/31
 * @since 1.0.0
 */
public interface AutowiredBeanBuilder<A> {

    Object build(A autowired, Class<?> beanType);
}
